package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.DrugAuditContextDTO;
import com.his.ai.dto.DrugAuditExecuteDTO;
import com.his.ai.dto.DrugAuditLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.DrugAuditCapability;
import com.his.ai.support.DrugHardRuleChecker;
import com.his.ai.vo.DrugAuditFindingVO;
import com.his.ai.vo.DrugAuditPromptVariablesVO;
import com.his.ai.vo.DrugAuditResultVO;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.support.ClinicalTextMatcher;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizClinicalRuleCheck;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.mapper.BizClinicalRuleCheckMapper;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientAllergy;
import com.his.patient.mapper.BizPatientAllergyMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysDrug;
import com.his.system.mapper.SysDrugMapper;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 处方合理性审核能力（P0-2）。
 * <p>
 * <b>两层结构，缺一不可</b>：
 * <ol>
 *   <li><b>硬规则层</b>（{@link DrugHardRuleChecker}）：确定性错误，可给出 errorLevel = 3，
 *       可拦截。规则能百分百命中的事交给概率模型是倒退。</li>
 *   <li><b>模型层</b>：规则写不完的长尾问题，只允许给 1 或 2，<b>永远不能拦截</b>。</li>
 * </ol>
 * <p>
 * <b>为什么模型不许给 3</b>：level 3 意味着「不让医生继续」，而模型有幻觉率。
 * 把一个概率系统的输出当作硬拦截条件，等于把医疗安全交给概率 ——
 * 这正是「工作流可以确定性编排，但结论权限必须分级」的具体落地。
 * 即便提示词已经禁止模型给 3，本类仍会强制把 3 压成 2：提示词是约束，不是保证。
 * <p>
 * <b>降级语义</b>：模型不可用时，<b>硬规则结果照常返回</b>，业务不受影响。
 * 与病历质控不同 —— 处方审核的降级不是「空白」，是「退回到传统规则审核系统」。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAuditCapabilityImpl implements DrugAuditCapability {

    private static final String TEMPLATE_NAME = "drug-audit";

    private static final String BIZ_TYPE = "prescription";

    private static final String RULE_NAME = "AI 处方合理性审核";

    /**
     * 与临床规则校验记录的规则类型既有口径一致：3-用药合理性
     */
    private static final int RULE_TYPE_MEDICATION = 3;

    /**
     * 沿用临床规则校验记录的校验结论口径：0-不通过 1-通过
     */
    private static final int RESULT_PASS = 1;

    private static final int RESULT_FAIL = 0;

    private static final int CHECK_STATUS_PENDING = 1;

    /**
     * 模型最多返回 5 条，与提示词约定一致
     */
    private static final int MAX_LLM_FINDINGS = 5;

    private static final int LEVEL_BLOCK = 3;

    private static final int ERROR_DETAIL_MAX_LENGTH = 1000;

    private static final int SUGGESTION_MAX_LENGTH = 500;

    private static final int OUTPUT_TOKEN_LIMIT = 1536;

    private final BizPrescriptionMapper bizPrescriptionMapper;

    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;

    private final BizMedicalRecordMapper bizMedicalRecordMapper;

    private final BizPatientMapper bizPatientMapper;

    private final BizPatientAllergyMapper bizPatientAllergyMapper;

    private final SysDrugMapper sysDrugMapper;

    private final BizClinicalRuleCheckMapper bizClinicalRuleCheckMapper;

    private final DrugHardRuleChecker drugHardRuleChecker;

    private final AiExecutionService aiExecutionService;

    private final RedisSequenceService redisSequenceService;

    private static void appendIfMeaningful(StringBuilder builder, String text, String fieldLabel) {
        if (!TextUtil.hasText(text)) {
            return;
        }
        if (ClinicalTextMatcher.isPlaceholderOnly(text, fieldLabel)) {
            return;
        }
        appendIfText(builder, text);
    }

    private static void appendIfText(StringBuilder builder, String text) {
        if (TextUtil.hasText(text)) {
            builder.append(text.replaceAll("\\s+", " ").trim()).append("；");
        }
    }

    /**
     * 把硬规则已命中的问题告诉模型，避免它换个说法再报一遍。
     * 重复报错会直接消耗医生的注意力，这是提示词里「不要重复」落到实处的关键。
     */
    private static String formatHardRuleHints(List<DrugAuditFindingVO> hardRuleFindings) {
        if (hardRuleFindings.isEmpty()) {
            return "（无）";
        }
        StringBuilder builder = new StringBuilder();
        for (DrugAuditFindingVO finding : hardRuleFindings) {
            builder.append("- [").append(finding.getCategory()).append("] ")
                    .append(finding.getErrorDetail()).append('\n');
        }
        return builder.toString().trim();
    }

    private static String formatPrescriptions(DrugAuditContextDTO context) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (BizPrescriptionDetail detail : context.details()) {
            SysDrug drug = detail.getDrugId() == null ? null : context.drugIndex().get(detail.getDrugId());
            builder.append(index++).append(". ").append(TextUtil.blankToDefault(detail.getDrugName(), "-"));
            if (TextUtil.hasText(detail.getSpecification())) {
                builder.append(' ').append(detail.getSpecification());
            }
            if (drug != null && TextUtil.hasText(drug.getGenericName())) {
                builder.append("（通用名：").append(drug.getGenericName()).append('）');
            }
            builder.append(" 单次剂量=").append(TextUtil.blankToDefault(detail.getSingleDosage(), "-"));
            builder.append(" 频次=").append(TextUtil.blankToDefault(detail.getFrequency(), "-"));
            builder.append(" 途径=").append(TextUtil.blankToDefault(detail.getRoute(), "-"));
            builder.append(" 疗程=").append(detail.getDuration() == null ? "-" : detail.getDuration() + "天");
            builder.append(" 数量=").append(detail.getQuantity() == null ? "-" : detail.getQuantity());
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    private static int clampLlmLevel(Integer level) {
        if (level == null) {
            return 1;
        }
        if (level >= LEVEL_BLOCK) {
            // 模型给了 3：降级为 2 并留日志。
            // 不是不信任提示词，是不把「拦截权」交给概率系统。
            log.warn("[AI-处方审核] 模型返回 errorLevel=3，已强制降为 2（拦截权仅属硬规则）");
            return 2;
        }
        return Math.max(1, level);
    }

    /**
     * 剔除模型提到的、处方里并不存在的药品名 —— 这是最容易被忽略的幻觉形态：
     * 问题描述看起来合理，但涉及的药根本没开。
     */
    private static String sanitizeRelatedDrugs(String relatedDrugs, Set<String> knownDrugNames) {
        if (!TextUtil.hasText(relatedDrugs)) {
            return "";
        }
        List<String> kept = new ArrayList<>();
        for (String token : relatedDrugs.split("[、,，;；/]")) {
            String name = token.trim();
            if (name.isEmpty()) {
                continue;
            }
            for (String known : knownDrugNames) {
                if (known.contains(name) || name.contains(known)) {
                    kept.add(known);
                    break;
                }
            }
        }
        if (kept.isEmpty()) {
            log.warn("[AI-处方审核] 模型提及的药品 {} 均不在处方中，已清空该字段", relatedDrugs);
            return "";
        }
        return String.join("、", new LinkedHashSet<>(kept));
    }

    private static Set<String> knownDrugNames(DrugAuditContextDTO context) {
        Set<String> names = new LinkedHashSet<>();
        for (BizPrescriptionDetail detail : context.details()) {
            if (TextUtil.hasText(detail.getDrugName())) {
                names.add(detail.getDrugName().trim());
            }
            SysDrug drug = detail.getDrugId() == null ? null : context.drugIndex().get(detail.getDrugId());
            if (drug != null && TextUtil.hasText(drug.getGenericName())) {
                names.add(drug.getGenericName().trim());
            }
        }
        return names;
    }

    private static String joinDetails(List<DrugAuditFindingVO> findings, int maxLength) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (DrugAuditFindingVO finding : findings) {
            builder.append(index++).append('.').append(tag(finding)).append(finding.getErrorDetail()).append('；');
        }
        return TextUtil.cut(builder.toString(), maxLength, "");
    }

    private static String joinSuggestions(List<DrugAuditFindingVO> findings, int maxLength) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (DrugAuditFindingVO finding : findings) {
            builder.append(index++).append('.').append(tag(finding)).append(finding.getSuggestion()).append('；');
        }
        return TextUtil.cut(builder.toString(), maxLength, "");
    }

    private static String tag(DrugAuditFindingVO finding) {
        String source = "HARD_RULE".equals(finding.getSource()) ? "[规则]" : "[AI]";
        int level = finding.getErrorLevel() == null ? 1 : finding.getErrorLevel();
        String levelText = level >= 3 ? "严重" : level == 2 ? "警告" : "提示";
        return source + "[" + levelText + "]";
    }

    private static List<DrugAuditFindingVO> dedupeAndSort(List<DrugAuditFindingVO> findings) {
        Map<String, DrugAuditFindingVO> unique = new LinkedHashMap<>();
        for (DrugAuditFindingVO finding : findings) {
            String key = finding.getCategory() + "|" + finding.getErrorDetail();
            DrugAuditFindingVO exists = unique.get(key);
            if (exists == null) {
                unique.put(key, finding);
            } else if (levelOf(exists) < levelOf(finding)) {
                unique.put(key, finding);
            }
        }
        List<DrugAuditFindingVO> result = new ArrayList<>(unique.values());
        result.sort(Comparator
                .comparingInt((DrugAuditFindingVO finding) -> levelOf(finding)).reversed()
                // 同级时硬规则优先展示：确定性结论应当排在模型推测之前
                .thenComparing(finding -> "HARD_RULE".equals(finding.getSource()) ? 0 : 1));
        return result;
    }

    private static int levelOf(DrugAuditFindingVO finding) {
        return finding.getErrorLevel() == null ? 0 : finding.getErrorLevel();
    }

    /**
     * 执行处方审核
     */
    public DrugAuditResultVO execute(DrugAuditExecuteDTO dto) {
        long start = System.currentTimeMillis();

        BizPrescription prescription = bizPrescriptionMapper.selectById(dto.getPrescriptionId());
        if (prescription == null) {
            throw new BusinessException("处方不存在或已作废：" + dto.getPrescriptionId());
        }

        DrugAuditContextDTO context = buildContext(prescription);
        if (context.details().isEmpty()) {
            throw new BusinessException("处方无明细，无法审核：" + prescription.getPrescriptionNo());
        }

        DrugAuditResultVO vo = new DrugAuditResultVO();

        // 第一层：硬规则。任何情况下都要跑 —— 它不依赖模型
        List<DrugAuditFindingVO> findings = new ArrayList<>(drugHardRuleChecker.check(context));
        vo.setHardRuleCount(findings.size());

        // 第二层：模型长尾审核
        Optional<DrugAuditLlmOutputDTO> llmOutput = callModel(prescription, context, findings);
        if (llmOutput.isPresent()) {
            List<DrugAuditFindingVO> llmFindings = toFindings(llmOutput.get(), context);
            vo.setLlmCount(llmFindings.size());
            findings.addAll(llmFindings);
        } else {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.DRUG_AUDIT)
                    + "；本次仅返回硬规则审核结果");
        }

        findings = dedupeAndSort(findings);
        vo.setFindings(findings);
        vo.setFindingCount(findings.size());
        vo.setMaxErrorLevel(findings.stream()
                .mapToInt(finding -> finding.getErrorLevel() == null ? 0 : finding.getErrorLevel())
                .max().orElse(0));
        // 只有硬规则能给出 3，因此「不通过」永远是确定性结论
        vo.setPass(vo.getMaxErrorLevel() < LEVEL_BLOCK);

        if (!Boolean.FALSE.equals(dto.getSaveResult())) {
            persist(prescription, vo);
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    /**
     * 一次性把审核需要的数据查齐。注意「只取审核必需的临床字段」——
     * 不查姓名以外的身份信息、不查身份证手机号，从源头减少脱敏压力。
     */
    private DrugAuditContextDTO buildContext(BizPrescription prescription) {
        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId()));

        Set<Long> drugIds = new LinkedHashSet<>();
        for (BizPrescriptionDetail detail : details) {
            if (detail.getDrugId() != null) {
                drugIds.add(detail.getDrugId());
            }
        }
        Map<Long, SysDrug> drugIndex = new LinkedHashMap<>();
        if (!drugIds.isEmpty()) {
            for (SysDrug drug : sysDrugMapper.selectBatchIds(drugIds)) {
                drugIndex.put(drug.getId(), drug);
            }
        }

        BizMedicalRecord record = prescription.getRecordId() == null
                ? null : bizMedicalRecordMapper.selectById(prescription.getRecordId());

        return new DrugAuditContextDTO(prescription, details, drugIndex, record,
                buildAllergyText(prescription, record),
                buildConditionText(prescription, record));
    }

    /**
     * 合并三处过敏信息：病历过敏史、患者档案过敏史、结构化过敏表。
     * <p>
     * 只看「有实质内容」的那些 —— 「无」「未见异常」「⚠ 过敏史 *」这类占位内容
     * 既不能证明过敏、也不能证明不过敏，混进来只会制造噪声。
     */
    private String buildAllergyText(BizPrescription prescription, BizMedicalRecord record) {
        StringBuilder builder = new StringBuilder();
        if (record != null) {
            appendIfMeaningful(builder, record.getAllergyHistory(), "过敏史");
        }

        BizPatient patient = prescription.getPatientId() == null
                ? null : bizPatientMapper.selectById(prescription.getPatientId());
        if (patient != null) {
            appendIfMeaningful(builder, patient.getAllergyHistory(), "过敏史");
        }

        if (prescription.getPatientId() != null) {
            List<BizPatientAllergy> allergies = bizPatientAllergyMapper.selectList(
                    new LambdaQueryWrapper<BizPatientAllergy>()
                            .eq(BizPatientAllergy::getPatientId, prescription.getPatientId()));
            for (BizPatientAllergy allergy : allergies) {
                appendIfMeaningful(builder, allergy.getAllergenName(), "过敏原");
            }
        }
        return builder.toString().trim();
    }

    /**
     * 禁忌人群比对用的文本：诊断 + 既往史。
     * <p>
     * 处方表的 gender/age/diagnosis 在演示库里大面积为空（49 张处方中 24 张无诊断），
     * 因此必须回落到病历表 —— 这是能不能审出问题的前提，不是可选的优化。
     */
    private String buildConditionText(BizPrescription prescription, BizMedicalRecord record) {
        StringBuilder builder = new StringBuilder();
        appendIfText(builder, prescription.getDiagnosis());
        if (record != null) {
            appendIfText(builder, record.getDiagnosis());
            appendIfText(builder, record.getDiagnosisName());
            appendIfText(builder, record.getPastHistory());
        }
        return builder.toString().trim();
    }

    private Optional<DrugAuditLlmOutputDTO> callModel(BizPrescription prescription, DrugAuditContextDTO context,
                                                      List<DrugAuditFindingVO> hardRuleFindings) {
        DrugAuditPromptVariablesVO variables = new DrugAuditPromptVariablesVO();
        variables.setGender(SysGenderEnum.getText(context.gender()));
        variables.setAge(context.age() == null ? "（未填写）" : context.age() + "岁");
        variables.setAllergyHistory(TextUtil.hasText(context.allergyText())
                ? context.allergyText() : "（无已知过敏史记录）");
        variables.setDiagnosis(TextUtil.hasText(context.conditionText())
                ? context.conditionText() : "（未填写）");
        variables.setPrescriptions(formatPrescriptions(context));
        variables.setHardRuleHints(formatHardRuleHints(hardRuleFindings));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.DRUG_AUDIT)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(prescription.getId())
                .inputDigest(formatPrescriptions(context) + " | " + variables.getDiagnosis())
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, DrugAuditLlmOutputDTO.class);
    }

    /**
     * 模型输出 → VO，并做三道净化
     */
    private List<DrugAuditFindingVO> toFindings(DrugAuditLlmOutputDTO output, DrugAuditContextDTO context) {
        List<DrugAuditFindingVO> findings = new ArrayList<>();
        if (output.getFindings() == null) {
            return findings;
        }
        Set<String> knownDrugNames = knownDrugNames(context);
        for (DrugAuditLlmOutputDTO.Finding raw : output.getFindings()) {
            if (findings.size() >= MAX_LLM_FINDINGS) {
                break;
            }
            if (!TextUtil.hasText(raw.getErrorDetail())) {
                continue;
            }
            DrugAuditFindingVO vo = new DrugAuditFindingVO();
            vo.setSource("LLM");
            // 净化一：模型永远不能给拦截级
            vo.setErrorLevel(clampLlmLevel(raw.getErrorLevel()));
            vo.setCategory(TextUtil.cut(raw.getCategory(), 40, "其他"));
            vo.setErrorDetail(TextUtil.cut(raw.getErrorDetail(), 200, ""));
            vo.setSuggestion(TextUtil.cut(raw.getSuggestion(), 200, ""));
            // 净化二：只保留处方里真实存在的药品名，模型幻想的药名一律剔除
            vo.setRelatedDrugs(sanitizeRelatedDrugs(raw.getRelatedDrugs(), knownDrugNames));
            vo.setEvidence("模型基于处方明细与诊断的合理性判断");
            findings.add(vo);
        }
        return findings;
    }

    /**
     * 写入临床规则校验记录，复用已有的医生工作站校验页。
     * <p>
     * 这样做的价值：AI 能力不需要新的前端页面就能被医生看到 ——
     * 结果落在原有的表里，原有的列表页、处理/忽略按钮全部可用。
     * <p>
     * 单号前缀刻意用 {@code RCAI}：与人工/既有校验（{@code RC}）区分开，
     * 既避免撞上校验单号的唯一索引，也便于事后统计 AI 的命中情况。
     */
    private void persist(BizPrescription prescription, DrugAuditResultVO vo) {
        if (prescription.getRecordId() == null) {
            log.info("[AI-处方审核] 处方 {} 未关联病历，跳过落库（biz_clinical_rule_check.record_id 非空）",
                    prescription.getPrescriptionNo());
            return;
        }
        try {
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            String operator = operatorUser.getRealName();
            BizClinicalRuleCheck check = new BizClinicalRuleCheck();
            check.setCheckNo(redisSequenceService.generateRuleCheckAiNo());
            check.setRecordId(prescription.getRecordId());
            check.setPatientId(prescription.getPatientId());
            check.setRuleType(RULE_TYPE_MEDICATION);
            check.setRuleName(RULE_NAME);
            check.setRuleContent(vo.isDegraded()
                    ? "硬规则审核（模型未参与）" : "硬规则审核 + 大模型长尾审核");
            check.setCheckResult(vo.isPass() ? RESULT_PASS : RESULT_FAIL);
            check.setErrorLevel(vo.getMaxErrorLevel() == 0 ? null : vo.getMaxErrorLevel());
            check.setErrorDetail(joinDetails(vo.getFindings(), ERROR_DETAIL_MAX_LENGTH));
            check.setSuggestion(joinSuggestions(vo.getFindings(), SUGGESTION_MAX_LENGTH));
            check.setCheckStatus(CHECK_STATUS_PENDING);
            check.setCheckBy(operator);
            check.setCheckTime(LocalDateTime.now());
            check.setCreateBy(operator);

            bizClinicalRuleCheckMapper.insert(check);
            vo.setCheckId(check.getId());
            vo.setCheckNo(check.getCheckNo());
        } catch (Exception ex) {
            // 审核结论已经拿到了，落库失败不该让医生看不到结论
            log.error("[AI-处方审核] 审核结果落库失败，结论仍正常返回", ex);
        }
    }
}
