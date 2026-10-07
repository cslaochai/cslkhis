package com.his.ai.service.impl;

import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.EmrQcExecuteDTO;
import com.his.ai.dto.EmrQcLlmOutputDTO;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.EmrQcCapability;
import com.his.ai.vo.EmrQcIssueVO;
import com.his.ai.vo.EmrQcResultVO;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.ClinicalTextMatcher;
import com.his.common.util.DateFormats;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizQualityControl;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizQualityControlMapper;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * 病历内涵质控能力（P0-3）。
 * <p>
 * <b>「内涵质控」与「形式质控」的分工</b>：
 * 形式质控（字段填没填、格式对不对）用规则就够了，不需要模型；
 * 内涵质控要判断的是「现病史写了但没写清楚」「诊断与查体对不上」这类问题，
 * 这是规则写不完的领域，才是模型的价值所在。
 * <p>
 * 但本能力仍然保留了一层<b>必填项硬规则</b>，原因不是为了省 token，
 * 而是为了<b>降级可用</b>：模型不可用时，质控不能变成一片空白。
 * 演示库里「主诉 *」「主诉」「现病史 *」这类套模板没替换内容的病历大量存在，
 * 硬规则单独就能抓出来 —— 这正是「降级不等于失效」的具体体现。
 * <p>
 * <b>本能力绝不做的事</b>：不修改病历、不阻断结诊、不给病历打不合格标记。
 * 质控意见只是「提示人工复核」，修改权与判断权始终在质控员手里。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmrQcCapabilityImpl implements EmrQcCapability {

    private static final String TEMPLATE_NAME = "emr-qc";

    private static final String BIZ_TYPE = "medical_record";

    /**
     * 质控类型：AI 内涵质控（综合）。
     * <p>
     * 既有 1/2/3 分别对应完整性/规范性/逻辑性的<b>形式</b>检查，
     * AI 内涵质控一次同时覆盖三个维度，因此单独立一个类型 4，
     * 避免把三个维度的结论硬塞进某一个既有类型里造成口径混乱。
     * 该取值已同步写入 sql 注释。
     */
    private static final int QC_TYPE_AI_INHERENT = 4;

    private static final int QC_RESULT_PASS = 1;

    private static final int QC_RESULT_FAIL = 0;

    private static final int QC_STATUS_PENDING = 1;

    /**
     * 模型最多返回 8 条，与提示词约定一致
     */
    private static final int MAX_LLM_ISSUES = 8;

    private static final int MAX_HARD_RULE_ISSUES = 8;

    private static final int SEVERITY_FATAL = 3;

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
            new RequiredFieldRule("主诉", BizMedicalRecord::getChiefComplaint, SEVERITY_FATAL),
            new RequiredFieldRule("现病史", BizMedicalRecord::getPresentIllness, SEVERITY_FATAL),
            new RequiredFieldRule("诊断", BizMedicalRecord::getDiagnosis, SEVERITY_FATAL),
            new RequiredFieldRule("既往史", BizMedicalRecord::getPastHistory, 2),
            new RequiredFieldRule("过敏史", BizMedicalRecord::getAllergyHistory, 2),
            new RequiredFieldRule("处理意见", BizMedicalRecord::getTreatmentPlan, 2),
            new RequiredFieldRule("专科检查", BizMedicalRecord::getSpecialistExam, 1));

    private final BizMedicalRecordMapper medicalRecordMapper;

    private final BizQualityControlMapper qualityControlMapper;

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
            if (!StringUtils.hasText(raw.getErrorDetail())) {
                continue;
            }
            EmrQcIssueVO issue = new EmrQcIssueVO();
            issue.setSource("LLM");
            // 维度取值必须归一，否则前端的维度筛选会漏掉拼错的值
            issue.setDimension(normalizeDimension(raw.getDimension()));
            issue.setSeverity(clampSeverity(raw.getSeverity()));
            issue.setFieldName(truncate(raw.getFieldName(), 30, "未指明字段"));
            issue.setErrorDetail(truncate(raw.getErrorDetail(), 200, ""));
            issue.setSuggestion(truncate(raw.getSuggestion(), 200, ""));
            issue.setEvidence(truncate(raw.getEvidence(), 80, ""));
            issues.add(issue);
        }
        return issues;
    }

    // 硬规则层：必填项

    private static String normalizeDimension(String dimension) {
        if (!StringUtils.hasText(dimension)) {
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
            return 1;
        }
        return Math.max(1, Math.min(SEVERITY_FATAL, severity));
    }

    private static String joinIssues(List<EmrQcIssueVO> issues, int maxLength) {
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (EmrQcIssueVO issue : issues) {
            String source = "HARD_RULE".equals(issue.getSource()) ? "[规则]" : "[AI]";
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
        return truncate(builder.toString(), maxLength, "");
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
                .thenComparing(issue -> "HARD_RULE".equals(issue.getSource()) ? 0 : 1));
        return result;
    }

    private static int severityOf(EmrQcIssueVO issue) {
        return issue.getSeverity() == null ? 0 : issue.getSeverity();
    }

    private static String buildNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateFormats.COMPACT_DATETIME);
        String tail = String.format("%06d", (int) (Math.random() * 1_000_000));
        return prefix + timestamp + tail;
    }

    private static String truncate(String text, int maxLength, String fallback) {
        if (!StringUtils.hasText(text)) {
            return fallback;
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "（未填写）";
    }

    /**
     * 执行病历内涵质控
     */
    public EmrQcResultVO execute(EmrQcExecuteDTO dto) {
        long start = System.currentTimeMillis();

        BizMedicalRecord record = medicalRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("病历不存在或已作废：" + dto.getRecordId());
        }

        EmrQcResultVO vo = new EmrQcResultVO();
        List<EmrQcIssueVO> issues = new ArrayList<>(checkRequiredFields(record));

        Optional<EmrQcLlmOutputDTO> llmOutput = callModel(record);
        if (llmOutput.isPresent()) {
            issues.addAll(toIssues(llmOutput.get()));
            vo.setSummary(truncate(llmOutput.get().getSummary(), 200, ""));
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
        vo.setPass(vo.getMaxSeverity() < SEVERITY_FATAL);

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
    private List<EmrQcIssueVO> checkRequiredFields(BizMedicalRecord record) {
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
            issue.setSource("HARD_RULE");
            issue.setDimension("completeness");
            issue.setSeverity(rule.severity());
            issue.setFieldName(rule.fieldName());
            issue.setErrorDetail(StringUtils.hasText(value)
                    ? String.format("「%s」仅有占位内容，未记录实质信息", rule.fieldName())
                    : String.format("「%s」未填写", rule.fieldName()));
            issue.setSuggestion(String.format("请补充%s的具体内容，避免使用「无」「正常」等笼统表述",
                    rule.fieldName()));
            issue.setEvidence(StringUtils.hasText(value)
                    ? truncate(value.replaceAll("\\s+", " ").trim(), 40, "")
                    : "（空）");
            issues.add(issue);
        }
        return issues;
    }

    private Optional<EmrQcLlmOutputDTO> callModel(BizMedicalRecord record) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("gender", SysGenderEnum.getText(record.getGender()));
        variables.put("age", record.getAge() == null ? "（未填写）" : record.getAge() + "岁");
        variables.put("chiefComplaint", nullToDash(record.getChiefComplaint()));
        variables.put("presentIllness", nullToDash(record.getPresentIllness()));
        variables.put("pastHistory", nullToDash(record.getPastHistory()));
        variables.put("personalHistory", nullToDash(record.getPersonalHistory()));
        variables.put("familyHistory", nullToDash(record.getFamilyHistory()));
        variables.put("allergyHistory", nullToDash(record.getAllergyHistory()));
        variables.put("generalCondition", nullToDash(record.getGeneralCondition()));
        variables.put("skinMucosa", nullToDash(record.getSkinMucosa()));
        variables.put("headNeck", nullToDash(record.getHeadNeck()));
        variables.put("chestLung", nullToDash(record.getChestLung()));
        variables.put("heart", nullToDash(record.getHeart()));
        variables.put("abdomen", nullToDash(record.getAbdomen()));
        variables.put("spineLimbs", nullToDash(record.getSpineLimbs()));
        variables.put("nervousSystem", nullToDash(record.getNervousSystem()));
        variables.put("specialistExam", nullToDash(record.getSpecialistExam()));
        variables.put("auxiliaryExam", nullToDash(record.getAuxiliaryExam()));
        variables.put("diagnosis", nullToDash(record.getDiagnosis()));
        variables.put("treatmentPlan", nullToDash(record.getTreatmentPlan()));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.EMR_QC)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(record.getId())
                // 只把主诉与诊断作为摘要 —— 病历全文很长，且没必要为了审计再存一遍
                .inputDigest(nullToDash(record.getChiefComplaint()) + " | " + nullToDash(record.getDiagnosis()))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, EmrQcLlmOutputDTO.class);
    }

    /**
     * 写入质控检查记录，复用已有质控列表页。
     * <p>
     * 单号前缀用 {@code QCAI}，与既有质控单号区分，避免撞质控单号的唯一索引。
     */
    private void persist(BizMedicalRecord record, EmrQcResultVO vo) {
        try {
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            String operator = operatorUser.getRealName();
            BizQualityControl qc = new BizQualityControl();
            qc.setQcNo(buildNo("QCAI"));
            qc.setRecordId(record.getId());
            qc.setPatientId(record.getPatientId());
            qc.setQcType(QC_TYPE_AI_INHERENT);
            qc.setQcContent("AI 病历内涵质控（完整性 + 规范性 + 逻辑性）");
            qc.setQcResult(vo.isPass() ? QC_RESULT_PASS : QC_RESULT_FAIL);
            qc.setErrorCount(vo.getIssueCount());
            qc.setErrorDetail(joinIssues(vo.getIssues(), ERROR_DETAIL_MAX_LENGTH));
            qc.setQcStatus(QC_STATUS_PENDING);
            qc.setQcBy(operator);
            qc.setQcTime(LocalDateTime.now());
            qc.setCreateBy(operator);
            qc.setRemark(vo.isDegraded() ? "模型未参与，仅必填项规则结果" : null);

            qualityControlMapper.insert(qc);
            vo.setQcId(qc.getId());
            vo.setQcNo(qc.getQcNo());
        } catch (Exception ex) {
            // 质控结论已经拿到了，落库失败不该让质控员看不到结论
            log.error("[AI-病历质控] 质控结果落库失败，结论仍正常返回", ex);
        }
    }

    /**
     * 必填项规则定义
     *
     * @param fieldName 字段中文名，同时用于识别「把字段名抄一遍」的伪填写
     * @param getter    字段取值器
     * @param severity  缺失时的严重程度
     */
    private record RequiredFieldRule(String fieldName,
                                     Function<BizMedicalRecord, String> getter,
                                     int severity) {
    }
}
