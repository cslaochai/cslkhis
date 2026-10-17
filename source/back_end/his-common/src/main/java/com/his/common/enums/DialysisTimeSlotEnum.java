package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 透析机位时段枚举
 */
@Getter
public enum DialysisTimeSlotEnum {

    MORNING(1, "上午"),
    AFTERNOON(2, "下午"),
    NIGHT(3, "夜间");

    private final int code;
    private final String label;

    DialysisTimeSlotEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DialysisTimeSlotEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DialysisTimeSlotEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DialysisTimeSlotEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DialysisTimeSlotEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DialysisTimeSlotEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
