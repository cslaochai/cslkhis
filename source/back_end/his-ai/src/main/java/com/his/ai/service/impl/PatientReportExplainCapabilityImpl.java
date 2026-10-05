package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.ai.constant.AiCapabilityKeys;
import com.his.ai.dto.AiCallDTO;
import com.his.ai.dto.PatientReportExplainDTO;
import com.his.ai.dto.PatientReportLlmOutputDTO;
import com.his.ai.entity.SysLabPlainItem;
import com.his.ai.mapper.SysLabPlainItemMapper;
import com.his.ai.service.AiExecutionService;
import com.his.ai.service.PatientReportExplainCapability;
import com.his.ai.support.PatientTextGuard;
import com.his.ai.vo.PatientLabItemPlainVO;
import com.his.ai.vo.PatientReportExplainVO;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.support.LabAbnormalJudge;
import com.his.medicaltech.support.LabCriticalValueRules;
import com.his.patient.service.PatientGuardianService;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 患者端报告解读（大白话版）。
 * <p>
 * <b>三层分工，越往上权限越小：</b>
 * <ol>
 *   <li><b>事实层（代码）</b>：哪些项异常、方向是偏高还是偏低、是否达到危急值、参考区间是什么。
 *       复用检验科已有的 {@link LabAbnormalJudge} / {@link LabCriticalValueRules} ——
 *       这些规则已经过检验科验证，患者端不该另起一套判定标准。</li>
 *   <li><b>白话层（词典）</b>：这项查什么、高/低通常意味着什么，来自 {@code sys_lab_plain_item}。
 *       人工维护、可审计、可改。<b>词典没有收录的项目，系统只说「需要医生判断」，绝不猜。</b></li>
 *   <li><b>表达层（模型，可选）</b>：只把上面两层串成一句通顺的总览。输出必须过
 *       {@link PatientTextGuard}，命中即整体丢弃回落到规则文案。</li>
 * </ol>
 * 这样切的直接好处是<b>降级可用</b>：模型挂了（现在就是这样 —— 112/129 次调用是「密钥未配置」），
 * 患者拿到的是一份措辞朴素但事实完整的解读，而不是一句「AI 服务不可用，请稍后再试」。
 * <p>
 * <b>为什么不做检查报告（CT/B超）的解读：</b>检查报告的核心是影像描述与结论，
 * 那是放射/超声医生写的专业文本，用词典改写成白话只会丢信息、还可能误导。
 * 患者看不懂应该去找医生，不是看一段被简化的文字。所以非检验报告直接明确报错。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientReportExplainCapabilityImpl implements PatientReportExplainCapability {

    private static final String TEMPLATE_NAME = "patient-report-explain";

    private static final String BIZ_TYPE = "report";

    /**
     * biz_report.report_type：1-检查 2-检验
     */
    private static final int REPORT_TYPE_LABORATORY = 2;

    /** 结果状态：正常 */
    private static final int STATUS_NORMAL = 1;
    /** 结果状态：偏高 */
    private static final int STATUS_HIGH = 2;
    /** 结果状态：偏低 */
    private static final int STATUS_LOW = 3;
    /** 结果状态：未判定 */
    private static final int STATUS_UNJUDGED = 4;
    /** 结果状态：异常（方向不明确） */
    private static final int STATUS_ABNORMAL = 5;

    private static final int OUTPUT_TOKEN_LIMIT = 512;

    private static final int SUMMARY_MAX_LENGTH = 120;

    /**
     * 固定免责提示。
     * <p>
     * 每一份患者可见的解读都必须带上它 —— 少了这句话，「血红蛋白偏低，提示贫血」
     * 就会被读成诊断结论。这句话不是形式主义，是这个能力能上线的<b>前提条件</b>。
     */
    private static final String ADVICE =
            "以上只是帮你读懂报告上的数字，不能代替医生的诊断。"
                    + "任何一项有疑问，请以接诊医生的解释为准；"
                    + "若结果标注为危急值，请立即联系医生或前往急诊。";

    /**
     * 词典未收录时的兜底文案。
     * <p>
     * 不写「可能是…」。写「可能是」患者的反应是去百度验证，而不是去问医生。
     */
    private static final String FALLBACK_ABNORMAL = "这项结果不在参考范围内，具体意味着什么需要医生结合你的情况判断。";

    private static final String FALLBACK_UNJUDGED = "这项系统没能自动判断（参考区间不可用），既不能算异常也不能算正常，请让医生核对。";

    private final BizReportMapper reportMapper;

    private final BizLaboratoryRecordMapper laboratoryRecordMapper;

    private final BizLabResultMapper labResultMapper;

    private final SysLabPlainItemMapper plainItemMapper;

    private final AiExecutionService aiExecutionService;

    private final PatientTextGuard textGuard;

    private final PatientGuardianService patientGuardianService;

    public PatientReportExplainVO execute(PatientReportExplainDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getPatientId() == null) {
            throw new BusinessException("未获取到就诊人身份，请重新登录");
        }

        BizReport report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new BusinessException("报告不存在：" + dto.getReportId());
        }
        if (patientGuardianService.patientScopeViolated(report.getPatientId())) {
            // 不区分「报告不存在」和「无权查看」，避免被用来探测报告是否存在
            throw new BusinessException("报告不存在或无权查看：" + dto.getReportId());
        }
        if (report.getReportType() == null || report.getReportType() != REPORT_TYPE_LABORATORY) {
            throw new BusinessException("目前只对检验报告提供逐项解读；检查报告（CT/B超等）请咨询接诊医生");
        }

        BizLaboratoryRecord record = report.getRecordId() == null
                ? null : laboratoryRecordMapper.selectById(report.getRecordId());
        if (record == null) {
            throw new BusinessException("该报告未关联检验记录，无法逐项解读");
        }
        List<BizLabResult> results = labResultMapper.selectList(
                new LambdaQueryWrapper<BizLabResult>()
                        .eq(BizLabResult::getRecordId, record.getId())
                        .orderByAsc(BizLabResult::getSortOrder)
                        .orderByAsc(BizLabResult::getId));
        if (results == null || results.isEmpty()) {
            throw new BusinessException("该检验记录尚未录入结果，无法解读");
        }

        Map<String, SysLabPlainItem> dict = loadDictionary();

        PatientReportExplainVO vo = new PatientReportExplainVO();
        vo.setReportId(String.valueOf(report.getId()));
        vo.setReportNo(report.getReportNo());
        vo.setItemName(record.getLaboratoryItemName());
        vo.setReportTime(report.getPublishTime() == null ? record.getExecuteTime() : report.getPublishTime());

        List<PatientLabItemPlainVO> items = new ArrayList<>();
        List<String> criticalNames = new ArrayList<>();
        int abnormalCount = 0;
        int unjudgedCount = 0;
        int criticalCount = 0;

        for (BizLabResult result : results) {
            PatientLabItemPlainVO item = new PatientLabItemPlainVO();
            item.setItemName(result.getLaboratoryItemName());
            item.setResultValue(result.getResultValue());
            item.setUnit(result.getResultUnit());
            item.setReferenceRange(result.getReferenceRange());

            Optional<LabCriticalValueRules.Hit> critical = LabCriticalValueRules.check(
                    result.getLaboratoryItemCode(), result.getLaboratoryItemName(),
                    result.getResultUnit(), result.getResultValue());
            boolean isCritical = critical.isPresent();
            item.setCritical(isCritical);
            critical.ifPresent(hit -> item.setCriticalDesc(hit.description()));

            boolean unjudged = LabAbnormalJudge.isUnjudged(result.getJudgeNote());
            Integer flag = result.getAbnormalFlag() == null ? LabAbnormalJudge.NORMAL : result.getAbnormalFlag();
            int status = toStatus(flag, unjudged);
            item.setStatus(status);
            item.setStatusText(statusText(status));
            item.setArrow(status == STATUS_HIGH ? "↑" : status == STATUS_LOW ? "↓" : "");

            if (unjudged) {
                unjudgedCount++;
            } else if (status != STATUS_NORMAL) {
                abnormalCount++;
            }
            if (isCritical) {
                criticalCount++;
                // 点名到项目：写「本次有 1 项达到危急值」患者还得自己去表里找是哪一项，
                // 而这条提示存在的唯一目的就是让他立刻行动
                if (StringUtils.hasText(result.getLaboratoryItemName())
                        && !criticalNames.contains(result.getLaboratoryItemName())) {
                    criticalNames.add(result.getLaboratoryItemName());
                }
            }

            SysLabPlainItem plain = StringUtils.hasText(result.getLaboratoryItemName())
                    ? dict.get(result.getLaboratoryItemName().trim()) : null;
            item.setPlainName(plain == null ? null : plain.getPlainName());
            item.setWhatIsIt(plain == null ? null : plain.getWhatIsIt());
            item.setPlainText(buildPlainText(status, unjudged, plain, critical.map(
                    LabCriticalValueRules.Hit::description).orElse(null)));

            items.add(item);
        }

        vo.setItems(items);
        vo.setItemCount(items.size());
        vo.setAbnormalCount(abnormalCount);
        vo.setUnjudgedCount(unjudgedCount);
        vo.setCriticalAlert(criticalCount > 0 ? buildCriticalAlert(criticalCount, criticalNames) : null);

        String ruleSummary = buildRuleSummary(items.size(), abnormalCount, unjudgedCount);
        vo.setSummary(ruleSummary);

        Optional<PatientReportLlmOutputDTO> llmOutput = callModel(record, items, ruleSummary);
        if (llmOutput.isPresent()) {
            String guarded = textGuard.guard(llmOutput.get().getSummary(),
                    AiCapabilityKeys.PATIENT_REPORT_EXPLAIN);
            if (StringUtils.hasText(guarded)) {
                vo.setSummary(truncate(guarded, SUMMARY_MAX_LENGTH));
                vo.setSource("model");
                vo.setDegraded(false);
            } else {
                // 模型给了一段越界文案：这正是闸门存在的意义 —— 宁可用朴素措辞，也不放无背书的诊断出去
                vo.setSource("rule");
                vo.setDegraded(true);
                vo.setDegradeReason("模型输出未通过患者文案安全闸，已回落到规则文案");
            }
        } else {
            vo.setSource("rule");
            vo.setDegraded(true);
            vo.setDegradeReason(aiExecutionService.degradeReasonOf(AiCapabilityKeys.PATIENT_REPORT_EXPLAIN)
                    + "；以下解读由检验规则与白话词典生成，不受影响");
        }

        vo.setAdvice(ADVICE);
        return vo;
    }

    // ---------------------------------------------------------------- 规则层

    private Map<String, SysLabPlainItem> loadDictionary() {
        List<SysLabPlainItem> all = plainItemMapper.selectList(
                new LambdaQueryWrapper<SysLabPlainItem>()
                        .eq(SysLabPlainItem::getStatus, 1));
        Map<String, SysLabPlainItem> map = new HashMap<>();
        if (all != null) {
            for (SysLabPlainItem item : all) {
                if (StringUtils.hasText(item.getItemName())) {
                    map.put(item.getItemName().trim(), item);
                }
            }
        }
        return map;
    }

    private static int toStatus(Integer flag, boolean unjudged) {
        if (unjudged) {
            return STATUS_UNJUDGED;
        }
        if (flag == null) {
            return STATUS_NORMAL;
        }
        return switch (flag) {
            case LabAbnormalJudge.HIGH -> STATUS_HIGH;
            case LabAbnormalJudge.LOW -> STATUS_LOW;
            case LabAbnormalJudge.ABNORMAL -> STATUS_ABNORMAL;
            default -> STATUS_NORMAL;
        };
    }

    private static String statusText(int status) {
        return switch (status) {
            case STATUS_HIGH -> "偏高";
            case STATUS_LOW -> "偏低";
            case STATUS_UNJUDGED -> "待核对";
            case STATUS_ABNORMAL -> "异常";
            default -> "正常";
        };
    }

    /**
     * 单项白话说明。
     * <p>
     * 危急值优先级最高：不管词典有没有，先把「立即联系医生」说在前面。
     * 这是代码判定的事实，不是措辞选择。
     */
    private static String buildPlainText(int status, boolean unjudged, SysLabPlainItem plain,
                                         String criticalDesc) {
        if (StringUtils.hasText(criticalDesc)) {
            return "【需要尽快处理】" + criticalDesc + "。请立即联系接诊医生或前往急诊。";
        }
        if (unjudged) {
            return FALLBACK_UNJUDGED;
        }
        if (status == STATUS_HIGH) {
            return plain == null ? FALLBACK_ABNORMAL : plain.getHighText();
        }
        if (status == STATUS_LOW) {
            return plain == null ? FALLBACK_ABNORMAL : plain.getLowText();
        }
        if (status == STATUS_ABNORMAL) {
            return plain == null ? FALLBACK_ABNORMAL : plain.getHighText();
        }
        // 正常：说清这项是查什么的，比说「一切正常」有用；也不必加任何判断
        if (plain != null && StringUtils.hasText(plain.getWhatIsIt())) {
            return "你的结果在参考范围内。这项是" + plain.getWhatIsIt() + "。";
        }
        return "你的结果在参考范围内。";
    }

    private static String buildCriticalAlert(int criticalCount, List<String> criticalNames) {
        StringBuilder builder = new StringBuilder("本次");
        for (String name : criticalNames) {
            builder.append('「').append(name).append('」');
        }
        builder.append(criticalCount > 1 ? " 共 " + criticalCount + " 项" : "");
        builder.append("达到危急值，请立即联系接诊医生或前往急诊。");
        return builder.toString();
    }

    private static String buildRuleSummary(int total, int abnormal, int unjudged) {
        StringBuilder builder = new StringBuilder();
        if (abnormal == 0 && unjudged == 0) {
            builder.append(String.format("这份报告共 %d 项，结果都在参考范围内。", total));
            return builder.toString();
        }
        builder.append(String.format("这份报告共 %d 项", total));
        if (abnormal > 0) {
            builder.append(String.format("，其中 %d 项不在参考范围内", abnormal));
        }
        if (unjudged > 0) {
            builder.append(String.format("，另有 %d 项需要医生核对", unjudged));
        }
        builder.append("。下面逐项说明每一项查的是什么、你的结果意味着什么。");
        return builder.toString();
    }

    // ---------------------------------------------------------------- 模型层

    private Optional<PatientReportLlmOutputDTO> callModel(BizLaboratoryRecord record,
                                                          List<PatientLabItemPlainVO> items,
                                                          String ruleSummary) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("itemName", nullToDash(record.getLaboratoryItemName()));
        variables.put("ruleSummary", ruleSummary);
        variables.put("items", renderItems(items));

        AiCallDTO call = AiCallDTO.builder()
                .capabilityKey(AiCapabilityKeys.PATIENT_REPORT_EXPLAIN)
                .templateName(TEMPLATE_NAME)
                .variables(variables)
                .bizType(BIZ_TYPE)
                .bizId(record.getId())
                .inputDigest(nullToDash(record.getPatientName()) + " | "
                        + nullToDash(record.getLaboratoryItemName()))
                .maxTokens(OUTPUT_TOKEN_LIMIT)
                .build();

        return aiExecutionService.call(call, PatientReportLlmOutputDTO.class);
    }

    private String renderItems(List<PatientLabItemPlainVO> items) {
        StringBuilder builder = new StringBuilder();
        for (PatientLabItemPlainVO item : items) {
            builder.append("- ").append(nullToDash(item.getItemName()));
            if (StringUtils.hasText(item.getPlainName())) {
                builder.append("（俗称").append(item.getPlainName()).append("）");
            }
            builder.append(" = ").append(nullToDash(item.getResultValue()));
            if (StringUtils.hasText(item.getUnit())) {
                builder.append(' ').append(item.getUnit());
            }
            builder.append("（参考 ").append(nullToDash(item.getReferenceRange()))
                    .append("，").append(item.getStatusText()).append("）");
            if (StringUtils.hasText(item.getPlainText())) {
                builder.append(" 已给出的白话说明：").append(item.getPlainText());
            }
            builder.append('\n');
        }
        return builder.length() == 0 ? "（无）" : builder.toString();
    }

    private static String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String value = text.trim();
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static String nullToDash(String text) {
        return StringUtils.hasText(text) ? text : "（未填写）";
    }
}
