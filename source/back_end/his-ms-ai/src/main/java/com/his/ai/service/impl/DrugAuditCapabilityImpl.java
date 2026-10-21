package com.his.ai.service.impl;

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
import com.his.common.enums.AiBasisSourceEnum;
import com.his.common.enums.AiBizTypeEnum;
import com.his.common.enums.CheckResultEnum;
import com.his.common.enums.ClinicalRuleErrorLevelEnum;
import com.his.common.enums.ClinicalRuleTypeEnum;
import com.his.common.enums.RuleCheckStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.ClinicalTextMatcher;
import com.his.common.util.TextUtil;
import com.his.spi.dto.AiClinicalRuleCheckSaveDTO;
import com.his.spi.service.AiEmrDataFacadeService;
import com.his.spi.service.DrugMasterFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.AiMedicalRecordContentVO;
import com.his.spi.vo.AiPrescriptionBriefVO;
import com.his.spi.vo.AiPrescriptionDetailRowVO;
import com.his.spi.vo.AiRuleCheckWriteVO;
import com.his.spi.vo.AllergyFactVO;
import com.his.spi.vo.DrugBriefVO;
import com.his.spi.vo.PatientMasterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 处方合理性审核能力（P0-2）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugAuditCapabilityImpl implements DrugAuditCapability {

    private static final String TEMPLATE_NAME = "drug-audit";

    private static final String RULE_NAME = "AI 处方合理性审核";

    /**
     * 模型最多返回 5 条，与提示词约定一致
     */
    private static final int MAX_LLM_FINDINGS = 5;

    private static final int ERROR_DETAIL_MAX_LENGTH = 1000;

    private static final int SUGGESTION_MAX_LENGTH = 500;

    private static final int OUTPUT_TOKEN_LIMIT = 1536;

    private final AiEmrDataFacadeService aiEmrDataFacadeService;

    private final DrugMasterFacadeService drugMasterFacadeService;

    private final PatientFacadeService patientFacadeService;

    private final DrugHardRuleChecker drugHardRuleChecker;

    private final AiExecutionService aiExecutionService;

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
        for (AiPrescriptionDetailRowVO detail : context.details()) {
            DrugBriefVO drug = detail.getDrugId() == null ? null : context.drugIndex().get(detail.getDrugId());
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
            return ClinicalRuleErrorLevelEnum.HINT.getCode();
        }
        if (level >= ClinicalRuleErrorLevelEnum.BLOCK.getCode()) {
            // 模型给了 3：降级为 2 并留日志。
            // 不是不信任提示词，是不把「拦截权」交给概率系统。
            log.warn("[AI-处方审核] 模型返回 errorLevel=3，已强制降为 2（拦截权仅属硬规则）");
            return ClinicalRuleErrorLevelEnum.WARN.getCode();
        }
        return Math.max(ClinicalRuleErrorLevelEnum.HINT.getCode(), level);
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
        for (AiPrescriptionDetailRowVO detail : context.details()) {
            if (TextUtil.hasText(detail.getDrugName())) {
                names.add(detail.getDrugName().trim());
            }
            DrugBriefVO drug = detail.getDrugId() == null ? null : context.drugIndex().get(detail.getDrugId());
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
        String source = AiBasisSourceEnum.HARD_RULE.getCode().equals(finding.getSource()) ? "[规则]" : "[AI]";
        int level = finding.getErrorLevel() == null
                ? ClinicalRuleErrorLevelEnum.HINT.getCode() : finding.getErrorLevel();
        return source + "[" + ClinicalRuleErrorLevelEnum.getLabel(level) + "]";
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
                .thenComparing(finding -> AiBasisSourceEnum.HARD_RULE.getCode().equals(finding.getSource()) ? 0 : 1));
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

        AiPrescriptionBriefVO prescription = aiEmrDataFacadeService.findPrescription(dto.getPrescriptionId());
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
        vo.setPass(vo.getMaxErrorLevel() < ClinicalRuleErrorLevelEnum.BLOCK.getCode());

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
    private DrugAuditContextDTO buildContext(AiPrescriptionBriefVO prescription) {
        List<AiPrescriptionDetailRowVO> details = aiEmrDataFacadeService.listPrescriptionDetails(prescription.getId());

        Set<Long> drugIds = new LinkedHashSet<>();
        for (AiPrescriptionDetailRowVO detail : details) {
            if (detail.getDrugId() != null) {
                drugIds.add(detail.getDrugId());
            }
        }
        Map<Long, DrugBriefVO> drugIndex = new LinkedHashMap<>();
        if (!drugIds.isEmpty()) {
            for (DrugBriefVO drug : drugMasterFacadeService.listDrugs(drugIds)) {
                drugIndex.put(drug.getId(), drug);
            }
        }

        AiMedicalRecordContentVO record = prescription.getRecordId() == null
                ? null : aiEmrDataFacadeService.findMedicalRecord(prescription.getRecordId());

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
    private String buildAllergyText(AiPrescriptionBriefVO prescription, AiMedicalRecordContentVO record) {
        StringBuilder builder = new StringBuilder();
        if (record != null) {
            appendIfMeaningful(builder, record.getAllergyHistory(), "过敏史");
        }

        PatientMasterVO patient = prescription.getPatientId() == null
                ? null : patientFacadeService.getPatient(prescription.getPatientId());
        if (patient != null) {
            appendIfMeaningful(builder, patient.getAllergyHistory(), "过敏史");
        }

        if (prescription.getPatientId() != null) {
            List<AllergyFactVO> allergies = patientFacadeService.listAllergies(prescription.getPatientId());
            for (AllergyFactVO allergy : allergies) {
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
    private String buildConditionText(AiPrescriptionBriefVO prescription, AiMedicalRecordContentVO record) {
        StringBuilder builder = new StringBuilder();
        appendIfText(builder, prescription.getDiagnosis());
        if (record != null) {
            appendIfText(builder, record.getDiagnosis());
            appendIfText(builder, record.getDiagnosisName());
            appendIfText(builder, record.getPastHistory());
        }
        return builder.toString().trim();
    }

    private Optional<DrugAuditLlmOutputDTO> callModel(AiPrescriptionBriefVO prescription, DrugAuditContextDTO context,
                                                      List<DrugAuditFindingVO> hardRuleFindings) {
        DrugAuditPromptVariablesVO variables = new DrugAuditPromptVariablesVO();
        variables.setGender(SysGenderEnum.getLabel(context.gender()));
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
                .bizType(AiBizTypeEnum.PRESCRIPTION.getCode())
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
            vo.setSource(AiBasisSourceEnum.LLM.getCode());
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
     * 通过 {@link AiEmrDataFacadeService#saveRuleCheck} 落库：核查单号、核查人与核查时间
     * 由病历域生成，AI 能力只交代结论本身，不直接碰病历表。
     */
    private void persist(AiPrescriptionBriefVO prescription, DrugAuditResultVO vo) {
        if (prescription.getRecordId() == null) {
            log.info("[AI-处方审核] 处方 {} 未关联病历，跳过落库（核查记录必须挂在病历上）",
                    prescription.getPrescriptionNo());
            return;
        }
        try {
            AiClinicalRuleCheckSaveDTO dto = new AiClinicalRuleCheckSaveDTO();
            dto.setRecordId(prescription.getRecordId());
            dto.setPatientId(prescription.getPatientId());
            dto.setRuleType(ClinicalRuleTypeEnum.MEDICATION_RATIONALITY.getCode());
            dto.setRuleName(RULE_NAME);
            dto.setRuleContent(vo.isDegraded()
                    ? "硬规则审核（模型未参与）" : "硬规则审核 + 大模型长尾审核");
            dto.setCheckResult(vo.isPass() ? CheckResultEnum.PASS.getCode() : CheckResultEnum.FAIL.getCode());
            dto.setErrorLevel(vo.getMaxErrorLevel() == 0 ? null : vo.getMaxErrorLevel());
            dto.setErrorDetail(joinDetails(vo.getFindings(), ERROR_DETAIL_MAX_LENGTH));
            dto.setSuggestion(joinSuggestions(vo.getFindings(), SUGGESTION_MAX_LENGTH));
            dto.setCheckStatus(RuleCheckStatusEnum.PENDING.getCode());

            AiRuleCheckWriteVO saved = aiEmrDataFacadeService.saveRuleCheck(dto);
            vo.setCheckId(saved.getId());
            vo.setCheckNo(saved.getCheckNo());
        } catch (Exception ex) {
            // 审核结论已经拿到了，落库失败不该让医生看不到结论
            log.error("[AI-处方审核] 审核结果落库失败，结论仍正常返回", ex);
        }
    }
}
