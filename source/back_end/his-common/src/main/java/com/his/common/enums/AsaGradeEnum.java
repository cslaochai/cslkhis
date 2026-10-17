package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * ASA 麻醉分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ 5-Ⅴ；Ⅴ 级 = 濒死、不接受手术基本活不过 24 小时）。
 */
@Getter
public enum AsaGradeEnum {

    I(1, "Ⅰ级"),
    II(2, "Ⅱ级"),
    III(3, "Ⅲ级"),
    IV(4, "Ⅳ级"),
    V(5, "Ⅴ级");

    private final int code;
    private final String label;

    AsaGradeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static AsaGradeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AsaGradeEnum item : values()) {
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
        AsaGradeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AsaGradeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AsaGradeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
