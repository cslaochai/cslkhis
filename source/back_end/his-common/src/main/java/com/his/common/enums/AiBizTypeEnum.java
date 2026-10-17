package com.his.common.enums;

import lombok.Getter;

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

    public static AiBizTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AiBizTypeEnum item : values()) {
            if (item.code.equals(code)) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(String code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(String code) {
        AiBizTypeEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(String code) {
        AiBizTypeEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
