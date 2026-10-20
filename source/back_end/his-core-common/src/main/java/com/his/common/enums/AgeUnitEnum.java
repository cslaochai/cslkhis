package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 年龄单位枚举
 */
@Getter
public enum AgeUnitEnum {

    YEAR(1, "岁"),
    MONTH(2, "月"),
    DAY(3, "天");

    private final int code;
    private final String label;

    AgeUnitEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AgeUnitEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AgeUnitEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AgeUnitEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AgeUnitEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AgeUnitEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
