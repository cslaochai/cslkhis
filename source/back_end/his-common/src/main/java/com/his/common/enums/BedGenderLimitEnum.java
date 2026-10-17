package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 床位性别限制枚举（0-不限 1-限男床 2-限女床）
 */
@Getter
public enum BedGenderLimitEnum {

    NONE(0, "不限"),
    MALE(1, "限男床"),
    FEMALE(2, "限女床");

    private final int code;
    private final String label;

    BedGenderLimitEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static BedGenderLimitEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (BedGenderLimitEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        BedGenderLimitEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        BedGenderLimitEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        BedGenderLimitEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
