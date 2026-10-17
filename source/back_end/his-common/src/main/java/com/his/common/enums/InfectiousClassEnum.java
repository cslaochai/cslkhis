package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 传染病类别枚举
 */
@Getter
public enum InfectiousClassEnum {

    CLASS_A(1, "甲类"),
    CLASS_B(2, "乙类"),
    CLASS_C(3, "丙类");

    private final int code;
    private final String label;

    InfectiousClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectiousClassEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InfectiousClassEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InfectiousClassEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InfectiousClassEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InfectiousClassEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
