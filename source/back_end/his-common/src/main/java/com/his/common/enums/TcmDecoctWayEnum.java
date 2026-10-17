package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 中药煎服方式枚举（1-代煎 2-自煎）。
 * <p>
 * 与煎药任务状态（待煎/已煎/已取/已作废）是两个不同口径：本枚举说的是「由谁煎」，
 * 那个说的是「煎到哪一步」，码值不可互相套用。
 */
@Getter
public enum TcmDecoctWayEnum {

    BY_HOSPITAL(1, "代煎"),
    SELF(2, "自煎");

    private final int code;
    private final String label;

    TcmDecoctWayEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static TcmDecoctWayEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TcmDecoctWayEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TcmDecoctWayEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TcmDecoctWayEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TcmDecoctWayEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
