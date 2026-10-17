package com.his.common.enums;

import lombok.Getter;

/**
 * 复诊收费策略的三态匹配条件（0-不限 1-要求相同 2-要求不同），
 * 「是否要求与原就诊同一医生」「是否要求与原就诊同一科室」两个条件共用这一套码值，
 * 具体比的是医生还是科室由策略行自己说明。
 */
@Getter
public enum RevisitFeeConditionEnum {

    ANY(0, "不限"),
    SAME(1, "要求相同"),
    DIFF(2, "要求不同");

    private final int code;
    private final String label;

    RevisitFeeConditionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RevisitFeeConditionEnum fromCode(int code) {
        for (RevisitFeeConditionEnum condition : values()) {
            if (condition.code == code) return condition;
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
        RevisitFeeConditionEnum condition = code == null ? null : fromCode(code);
        return condition == null ? "" : condition.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        RevisitFeeConditionEnum condition = code == null ? null : fromCode(code);
        if (condition != null) {
            return condition.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
