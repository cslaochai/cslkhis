package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 死亡地点枚举
 */
@Getter
public enum DeathPlaceEnum {

    HOSPITAL(1, "医院"),
    TRANSFER(2, "来院途中"),
    HOME(3, "家中"),
    CIVIL_AGENCY(4, "民政管理机构"),
    OTHER_INSTITUTION(5, "其他机构"),
    UNSPECIFIED(9, "未指明");

    private final int code;
    private final String label;

    DeathPlaceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static DeathPlaceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeathPlaceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeathPlaceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeathPlaceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeathPlaceEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
