package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 麻醉随访恢复情况（1-良好 2-一般 3-差）。
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

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AnesthesiaFollowupRecoveryEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AnesthesiaFollowupRecoveryEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AnesthesiaFollowupRecoveryEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AnesthesiaFollowupRecoveryEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AnesthesiaFollowupRecoveryEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
