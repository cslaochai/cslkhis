package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 手术/操作级别枚举（1~4 级，四级风险最高）。
 */
@Getter
public enum TechLevelEnum {

    LEVEL_1(1, "一级"),
    LEVEL_2(2, "二级"),
    LEVEL_3(3, "三级"),
    LEVEL_4(4, "四级");

    private final int code;
    private final String label;

    TechLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TechLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TechLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TechLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TechLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TechLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
