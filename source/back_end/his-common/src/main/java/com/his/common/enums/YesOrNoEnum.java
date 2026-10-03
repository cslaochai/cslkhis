package com.his.common.enums;

import lombok.Getter;

/**
 * 是否标志枚举（1-是 0-否），所有「是否 xxx」的 0/1 标志位共用
 */
@Getter
public enum YesOrNoEnum {

    YES(1, "是"),
    NO(0, "否");

    private final int code;
    private final String label;

    YesOrNoEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static YesOrNoEnum fromCode(int code) {
        for (YesOrNoEnum flag : values()) {
            if (flag.code == code) return flag;
        }
        return null;
    }
}
