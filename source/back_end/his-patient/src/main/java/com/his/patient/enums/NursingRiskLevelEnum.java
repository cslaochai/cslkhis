package com.his.patient.enums;

import lombok.Getter;

/**
 * 护理评估风险等级枚举（biz_nursing_assessment.risk_level：1-低 2-中 3-高 4-极高）
 */
@Getter
public enum NursingRiskLevelEnum {

    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险"),
    EXTREME(4, "极高风险");

    private final int code;
    private final String label;

    NursingRiskLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingRiskLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NursingRiskLevelEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」——
     * 脏数据应由数据治理流程修复，而非界面伪装。
     */
    public static String getText(Integer code) {
        NursingRiskLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，
     * 用于业务异常消息或审计日志，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        NursingRiskLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
