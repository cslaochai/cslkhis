package com.his.common.enums;

import lombok.Getter;

/**
 * 统一性别枚举（字典性别字典：1-男 2-女 9-未知）
 */
@Getter
public enum SysGenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(9, "未知");

    private final int code;
    private final String label;

    SysGenderEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SysGenderEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysGenderEnum g : values()) {
            if (g.code == code) {
                return g;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
