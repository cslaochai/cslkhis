package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 器械清点阶段（0-未开始 1-术前清点完成 2-关体前清点完成 3-关体后清点完成）。
 */
@Getter
public enum CountPhaseEnum {

    NONE(0, "未开始"),
    BEFORE(1, "术前清点完成"),
    CLOSURE(2, "关体前清点完成"),
    FINAL(3, "关体后清点完成");

    private final int code;
    private final String label;

    CountPhaseEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static CountPhaseEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CountPhaseEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        CountPhaseEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CountPhaseEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CountPhaseEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
