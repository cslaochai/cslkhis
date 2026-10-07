package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位匹配等级（按申请科室与床位类型现算，不落库）
 */
@Getter
public enum BedMatchLevelEnum {

    SAME_DEPT_SAME_TYPE(1, "同科同型"),
    SAME_DEPT_OTHER_TYPE(2, "同科异型"),
    CROSS_DEPT_SAME_TYPE(3, "跨科同型"),
    CROSS_DEPT_OTHER_TYPE(4, "跨科异型");

    private final Integer code;
    private final String label;

    BedMatchLevelEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedMatchLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BedMatchLevelEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用码值 → 文案。脏值保留原始码值，绝不喂前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        BedMatchLevelEnum item = fromCode(code);
        if (item != null) {
            return item.label;
        }
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
