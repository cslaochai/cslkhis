package com.his.common.enums;

import lombok.Getter;

/**
 * 删除标志枚举
 */
@Getter
public enum DelFlagEnum {

    NORMAL(0, "正常"),
    DELETED(1, "已删除");

    private final int code;
    private final String label;

    DelFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DelFlagEnum fromCode(int code) {
        for (DelFlagEnum flag : values()) {
            if (flag.code == code) return flag;
        }
        return null;
    }
}
