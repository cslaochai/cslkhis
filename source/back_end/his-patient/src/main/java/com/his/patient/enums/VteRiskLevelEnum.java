package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE 风险等级枚举（Caprini 评分分档）
 */
@Getter
public enum VteRiskLevelEnum {

    LOW(1, "低风险"),
    MIDDLE(2, "中风险"),
    HIGH(3, "高风险"),
    EXTREME(4, "极高风险");

    private final int code;
    private final String label;

    VteRiskLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteRiskLevelEnum fromCode(int code) {
        for (VteRiskLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String labelOf(Integer code) {
        VteRiskLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        VteRiskLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
