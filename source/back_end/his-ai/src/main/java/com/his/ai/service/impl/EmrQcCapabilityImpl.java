package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.EmrQcExecuteDTO;
import com.his.ai.dto.EmrQcLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.EmrQcCapability;
import com.his.ai.vo.EmrQcIssueVO;
import com.his.ai.vo.EmrQcPromptVariablesVO;
import com.his.ai.vo.EmrQcResultVO;
import com.his.common.enums.AiBasisSourceEnum;
import com.his.common.enums.AiBizTypeEnum;
import com.his.common.enums.CheckResultEnum;
import com.his.common.enums.QcSeverityEnum;
import com.his.common.enums.RecordQcTypeEnum;
import com.his.common.enums.RuleCheckStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.ClinicalTextMatcher;
import com.his.common.util.TextUtil;
import com.his.spi.dto.AiQualityControlSaveDTO;
import com.his.spi.service.AiEmrDataFacadeService;
import com.his.spi.vo.AiMedicalRecordContentVO;
import com.his.spi.vo.AiQualityControlWriteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

/**
 * 病历内涵质控能力（P0-3）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrQcCapabilityImpl         implements EmrQcCapability {

    private static final String TEMPLATE_NAME = "emr-qc";

    /**
     * 模型最多返回 8 条，与提示词约定一致
     */
    private static final int MAX_LLM_ISSUES = 8;

    private static final int MAX_HARD_RULE_ISSUES = 8;

    private static final int ERROR_DETAIL_MAX_LENGTH = 1000;

    private static final int OUTPUT_TOKEN_LIMIT = 2048;

    /**
     * 必填项硬规则：字段取值器 + 中文名 + 缺失时的严重程度。
     * <p>
     * 严重程度的口径：主诉/现病史/诊断缺失 → 3（病历不成立）；
     * 既往史/过敏史/处理意见缺失 → 2（影响诊疗安全性）；
     * 专科检查缺失 → 1（可接受但不规范）。
     */
    private static final List<RequiredFieldRule> REQUIRED_FIELDS = List.of(
            new RequiredFieldRule("主诉", AiMedicalRecordContentVO::getChiefComplaint, QcSeverityEnum.FATAL.getCode()),
            new RequiredFieldRule("现病史", AiMedicalRecordContentVO::getPresentIllness, QcSeverityEnum.FATAL.getCode()),
            new RequiredFieldRule("诊断", AiMedicalRecordContentVO::getDiagnosis, QcSeverityEnum.FATAL.getCode()),
            new RequiredFieldRule("既往史", AiMedicalRecordContentVO::getPastHistory, QcSeverityEnum.MAJOR.getCode()),
            new RequiredFieldRule("过敏史", AiMedicalRecordContentVO::getAllergyHistory, QcSeverityEnum.MAJOR.getCode()),
            new RequiredFieldRule("处理意见", AiMedicalRecordContentVO::getTreatmentPlan, QcSeverityEnum.MAJOR.getCode()),
            new RequiredFieldRule("专科检查", AiMedicalRecordContentVO::getSpecialistExam, QcSeverityEnum.MINOR.getCode()));

    private final AiEmrDataFacadeService aiEmrDataFacadeService;

    private final AiExecutionService aiExecutionService;

    private static List<EmrQcIssueVO> toIssues(EmrQcLlmOutputDTO output) {
        List<EmrQcIssueVO> issues = new ArrayList<>();
        if (output.getIssues() == null) {
            return issues;
        }
        for (EmrQcLlmOutputDTO.Issue raw : output.getIssues()) {
            if (issues.size() >= MAX_LLM_ISSUES) {
                break;
            }
            if (!TextUtil.hasText(raw.getErrorDetail())) {
                continue;
            }
            EmrQcIssueVO issue = new EmrQcIssueVO();
            issue.setSource(AiBasisSourceEnum.LLM.getCode());
            // 维度取值必须归一，否则前端的维度筛选会漏掉拼错的值
            issue.setDimension(normalizeDimension(raw.getDimension()));
            issue.setSeverity(clampSeverity(raw.getSeverity()));
            issue.setFieldName(TextUtil.cut(raw.getFieldName(), 30, "未指明字段"));
            issue.setErrorDetail(TextUtil.cut(raw.getErrorDetail(), 200, ""));
            issue.setSuggestion(TextUtil.cut(raw.getSuggestion(), 200, ""));
            issue.setEvidence(TextUtil.cut(raw.getEvidence(), 80, ""));
            issues.add(issue);
        }
        return issues;
    }

    // 硬规则层：必填项

    private static String normalizeDimension(String dimension) {
        if (!TextUtil.hasText(dimension)) {
            return "completeness";
        }
        String value = dimension.trim().toLowerCase();
        return switch (value) {
            case "regularity", "logic", "completeness" -> value;
            default -> "completeness";
        };
    }

    private static int clampSeverity(Integer severity) {
        if (severity == null) {
            return QcSeverityEnum.MINOR.getCode();
        }
        return Math.max(QcSeverityEnum.MINOR.getCode(),
                Math.min(QcSeverityEnum.FATAL.getCode(), severity));
    }

    private static String joinIssues(List<EmrQcIssueVO> issues, int maxLength) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (EmrQcIssueVO issue : issues) {
            String source = AiBasisSourceEnum.HARD_RULE.getCode().equals(issue.getSource()) ? "[规则]" : "[AI]";
            String dimension = switch (issue.getDimension() == null ? "" : issue.getDimension()) {
                case "regularity" -> "规范性";
                case "logic" -> "逻辑性";
                default -> "完整性";
            };
            builder.append(index++).append('.').append(source)
                    .append('[').append(dimension).append("]")
                    .append(issue.getFieldName()).append('：')
                    .append(issue.getErrorDetail()).append('；');
        }
        return TextUtil.cut(builder.toString(), maxLength, "");
    }

    private static List<EmrQcIssueVO> dedupeAndSort(List<EmrQcIssueVO> issues) {
        Map<String, EmrQcIssueVO> unique = new LinkedHashMap<>();
        for (EmrQcIssueVO issue : issues) {
            String key = issue.getDimension() + "|" + issue.getFieldName() + "|" + issue.getErrorDetail();
            EmrQcIssueVO exists = unique.get(key);
            if (exists == null || severityOf(exists) < severityOf(issue)) {
                unique.put(key, issue);
            }
        }
        List<EmrQcIssueVO> result = new ArrayList<>(unique.values());
        result.sort(Comparator
                .comparingInt(EmrQcIssueVO::getSeverity).reversed()
                // 同级时硬规则优先：确定性结论排在模型推测之前
                .thenComparing(issue -> AiBasisSourceEnum.HARD_RULE.getCode().equals(issue.getSource()) ? 0 : 1));
        return result;
    }

    private static int severityOf(EmrQcIssueVO issue) {
        return issue.getSeverity() == null ? 0 : issue.getSeverity();
    }

    /**
     * 执行病历内涵质控
     */
    public EmrQcResultVO execute(EmrQcExecuteDTO dto) {
        long start = System.currentTimeMillis();

        AiMedicalRecordContentVO record = aiEmrDataFacadeService.findMedicalRecord(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("病历不存在或已作废：" + dto.getRecordId());
        }

        EmrQcResultVO vo = new EmrQcResultVO();
        List<EmrQcIssueVO> issues = new ArrayList<>(checkRequiredFields(record));

        Optional<EmrQcLlmOutputDTO> llmOutput = callModel(record);
        if (llmOutput.isPresent()) {
            issues.addAll(toIssues(llmOutput.get()));
            vo.setSummary(TextUtil.cut(llmOutput.get().getSummary(), 200, ""));
        } else {
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.EMR_QC)
                    + "；本次仅返回必填项规则检查结果");
            vo.setSummary("大模型内涵质控未执行，以下仅为必填项规则检查结果。");
        }

        issues = dedupeAndSort(issues);
        vo.setIssues(issues);
        vo.setIssueCount(issues.size());
        vo.setMaxSeverity(issues.stream()
                .mapToInt(issue -> issue.getSeverity() == null ? 0 : issue.getSeverity())
                .max().orElse(0));
        vo.setPass(vo.getMaxSeverity() < QcSeverityEnum.FATAL.getCode());

        if (!Boolean.FALSE.equals(dto.getSaveResult())) {
            persist(record, vo);
        }

        vo.setLatencyMs(System.currentTimeMillis() - start);
        return vo;
    }

    /**
     * 必填项检查。注意判据不是「字段是否为空」，而是「字段是否有实质内容」——
     * 「无」「未见异常」以及「把字段名抄一遍」都算没写。
     */
    private List<EmrQcIssueVO> checkRequiredFields(AiMedicalRecordContentVO record) {
        List<EmrQcIssueVO> issues = new ArrayList<>();
        for (RequiredFieldRule rule : REQUIRED_FIELDS) {
            if (issues.size() >= MAX_HARD_RULE_ISSUES) {
                break;
            }
            String value = rule.getter().apply(record);
            if (!ClinicalTextMatcher.isPlaceholderOnly(value, rule.fieldName())) {
                continue;
            }
            EmrQcIssueVO issue = new EmrQcIssueVO();
            issue.setSource(AiBasisSourceEnum.HARD_RULE.getCode());
            issue.setDimension("completeness");
            issue.setSeverity(rule.severity());
            issue.setFieldName(rule.fieldName());
            issue.setErrorDetail(TextUtil.hasText(value)
                    ? String.format("「%s」仅有占位内容，未记录实质信息", rule.fieldName())
                    : String.format("「%s」未填写", rule.fieldName()));
            issue.setSuggestion(String.format("请补充%s的具体内容，避免使用「无」「正常」等笼统表述",
                    rule.fieldName()));
            issue.setEvidence(TextUtil.hasText(value)
                    ? TextUtil.cut(value.replaceAll("\\s+", " ").trim(), 40, "")
                    : "（空）");
            issues.add(issue);
        }
        return issues;
    }

    private Optional<EmrQcLlmOutputDTO> callModel(AiMedicalRecordContentVO record) {
        EmrQcPromptVariablesVO variables = new EmrQcPromptVariablesVO();
        variables.setGender(SysGenderEnum.getLabel(record.getGender()));
        variables.setAge(record.getAge() == null ? "（未填写）" : record.getAge() + "岁");
        variables.setChiefComplaint(TextUtil.blankToDefault(record.getChiefComplaint(), "（未填写）"));
        variables.setPresentIllness(TextUtil.blankToDefault(record.getPresentIllness(), "（未填写）"));
        variables.setPastHistory(TextUtil.blankToDefault(record.getPastHistory(), "（未填写）"));
        variables.setPersonalHistory(TextUtil.blankToDefault(record.getPersonalHistory(), "（未填写）"));
        variables.setFamilyHistory(TextUtil.blankToDefault(record.getFamilyHistory(), "（未填写）"));
        variables.setAllergyHistory(TextUtil.blankToDefault(record.getAllergyHistory(), "（未填写）"));
        variables.setGeneralCondition(TextUtil.blankToDefault(record.getGeneralCondition(), "（未填写）"));
        variables.setSkinMucosa(TextUtil.blankToDefault(record.getSkinMucosa(), "（未填写）"));
        variables.setHeadNeck(TextUtil.blankToDefault(record.getHeadNeck(), "（未填写）"));
        variables.setChestLung(TextUtil.blankToDefault(record.getChestLung(), "（未填写）"));
        variables.setHeart(TextUtil.blankToDefault(record.getHeart(), "（未填写）"));
        variables.setAbdomen(TextUtil.blankToDefault(record.getAbdomen(), "（未填写）"));
        variables.setSpineLimbs(TextUtil.blankToDefault(record.getSpineLimbs(), "（未填写）"));
        variables.setNervousSystem(TextUtil.blankToDefault(record.getNervousSystem(), "（未填写）"));
        variables.setSpecialistExam(TextUtil.blankToDefault(record.getSpecialistExam(), "（未填写）"));
        variables.setAuxiliaryExam(TextUtil.blankToDefault(record.getAuxiliaryExam(), "（未填写）"));
        variables.setDiagnosis(TextUtil.blankToDefault(record.getDiagnosis(), "（未填写）"));
        variables.setTreatmentPlan(TextUtil.blankToDefault(record.getTreatmentPlan(), "（未填写）"));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.EMR_QC)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(AiBizTypeEnum.MEDICAL_RECORD.getCode())
                .bizId(record.getId())
                // 只把主诉与诊断作为摘要 —— 病历全文很长，且没必要为了审计再存一遍
                .inputDigest(TextUtil.blankToDefault(record.getChiefComplaint(), "（未填写）") + " | " + TextUtil.blankToDefault(record.getDiagnosis(), "（未填写）"))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, EmrQcLlmOutputDTO.class);
    }

    /**
     * 写入质控检查记录，复用已有质控列表页。
     * <p>
     * 通过 {@link AiEmrDataFacadeService#saveQualityControl} 落库：质控单号、质控人与质控时间
     * 由病历域生成，AI 能力只交代结论本身。
     */
    private void persist(AiMedicalRecordContentVO record, EmrQcResultVO vo) {
        try {
            AiQualityControlSaveDTO dto = new AiQualityControlSaveDTO();
            dto.setRecordId(record.getId());
            dto.setPatientId(record.getPatientId());
            dto.setQcType(RecordQcTypeEnum.AI_INHERENT.getCode());
            dto.setQcContent("AI 病历内涵质控（完整性 + 规范性 + 逻辑性）");
            dto.setQcResult(vo.isPass() ? CheckResultEnum.PASS.getCode() : CheckResultEnum.FAIL.getCode());
            dto.setErrorCount(vo.getIssueCount());
            dto.setErrorDetail(joinIssues(vo.getIssues(), ERROR_DETAIL_MAX_LENGTH));
            dto.setQcStatus(RuleCheckStatusEnum.PENDING.getCode());
            dto.setRemark(vo.isDegraded() ? "模型未参与，仅必填项规则结果" : null);

            AiQualityControlWriteVO saved = aiEmrDataFacadeService.saveQualityControl(dto);
            vo.setQcId(saved.getId());
            vo.setQcNo(saved.getQcNo());
        } catch (Exception ex) {
            // 质控结论已经拿到了，落库失败不该让质控员看不到结论
            log.error("[AI-病历质控] 质控结果落库失败，结论仍正常返回", ex);
        }
    }

    /**
     * 必填项规则定义
     */
    private record RequiredFieldRule(String fieldName,
                                     Function<AiMedicalRecordContentVO, String> getter,
                                     int severity) {
    }
}
