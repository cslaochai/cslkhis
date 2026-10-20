package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门诊分诊等级（区别于急诊 I~IV 级）。
 */
@Getter
public enum TriageLevelEnum {

    CRITICAL(1, "1级·危重"),
    EMERGENCY(2, "2级·急症"),
    URGENT(3, "3级·亚急"),
    NON_URGENT(4, "4级·非急");

    private final Integer code;
    private final String desc;

    TriageLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TriageLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TriageLevelEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TriageLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.desc;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TriageLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.desc;
    }

    public static String getLabelOrUnknown(Integer code) {
        TriageLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.desc;
    }
}
