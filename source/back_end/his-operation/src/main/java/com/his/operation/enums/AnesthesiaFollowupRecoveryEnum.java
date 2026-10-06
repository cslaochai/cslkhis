package com.his.operation.enums;

import lombok.Getter;

/**
 * 麻醉随访恢复情况（1-良好 2-一般 3-差）。
 *
 * <p>码值→文案的唯一出口（原 {@code FollowupAdverseItems#recoveryText} 已上移至此）。
 */
@Getter
public enum AnesthesiaFollowupRecoveryEnum {

    GOOD(1, "良好"),
    FAIR(2, "一般"),
    POOR(3, "差");

    private final int code;
    private final String label;

    AnesthesiaFollowupRecoveryEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnesthesiaFollowupRecoveryEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AnesthesiaFollowupRecoveryEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        AnesthesiaFollowupRecoveryEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        AnesthesiaFollowupRecoveryEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }
}
