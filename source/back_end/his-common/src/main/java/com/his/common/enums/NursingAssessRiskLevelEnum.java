package com.his.common.enums;

import lombok.Getter;

/**
 * 护理风险评估等级枚举（1-低风险 2-中风险 3-高风险 4-极高风险）。
 * <p>
 * 等级由评估总分按分数段现算，3 及以上才算「高风险」，交班与预警口径都取这一档。
 */
@Getter
public enum NursingAssessRiskLevelEnum {

    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险"),
    EXTREME(4, "极高风险");

    private final int code;
    private final String label;

    NursingAssessRiskLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static NursingAssessRiskLevelEnum fromCode(int code) {
        for (NursingAssessRiskLevelEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        NursingAssessRiskLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        NursingAssessRiskLevelEnum item = code == null ? null : fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
