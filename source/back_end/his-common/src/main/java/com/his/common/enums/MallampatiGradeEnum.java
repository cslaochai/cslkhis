package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Mallampati 气道分级
 */
@Getter
public enum MallampatiGradeEnum {

    CLASS_1(1, "Ⅰ"),
    CLASS_2(2, "Ⅱ"),
    CLASS_3(3, "Ⅲ"),
    CLASS_4(4, "Ⅳ");

    private final Integer code;
    private final String label;

    MallampatiGradeEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static MallampatiGradeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (MallampatiGradeEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        MallampatiGradeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        MallampatiGradeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        MallampatiGradeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
