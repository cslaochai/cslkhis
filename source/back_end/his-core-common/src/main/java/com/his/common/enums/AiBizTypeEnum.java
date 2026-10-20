package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * AI 调用业务类型枚举（字符串码值，审计检索用）。
 * <p>
 * 与站内信的业务类型是两个不同口径，不要互相套用。
 */
@Getter
public enum AiBizTypeEnum {

    MEDICAL_RECORD("medical_record", "病历"),
    PRESCRIPTION("prescription", "处方"),
    LABORATORY_RECORD("laboratory_record", "检验结果"),
    REPORT("report", "患者报告解读"),
    OPERATION_QA("operation_qa", "运营问数"),
    EMERGENCY("emergency", "急诊分诊"),
    TRIAGE("triage", "导诊归一"),
    KNOWLEDGE("knowledge", "知识库问答"),
    PREVISIT("previsit", "预问诊"),
    FOLLOWUP("followup", "随访话术"),
    INSURANCE_AUDIT("insurance_audit", "医保审核"),
    DETERIORATION_ALERT("deterioration_alert", "危重预警"),
    NURSING_HANDOVER("nursing_handover", "护理交接班");

    private final String code;
    private final String label;

    AiBizTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return code != null && fromCode(code) != null;
    }

    public static AiBizTypeEnum fromCode(String code) {
        if (null == code) {
            return null;
        }
        for (AiBizTypeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(String code) {
        AiBizTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(String code, String defaultValue) {
        AiBizTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(String code) {
        AiBizTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
