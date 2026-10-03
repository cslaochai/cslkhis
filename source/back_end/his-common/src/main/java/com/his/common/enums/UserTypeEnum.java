package com.his.common.enums;

import lombok.Getter;

/**
 * 删除标志枚举
 */
@Getter
public enum UserTypeEnum {

    INNER(1, "院内用户"),
    OTHER_DOCTOR(2, "院外用户"),
    PATIENT(3, "患者"),
    OTHOR(4, "其他");

    private final int code;
    private final String label;

    UserTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static UserTypeEnum fromCode(int code) {
        for (UserTypeEnum flag : values()) {
            if (flag.code == code) return flag;
        }
        return null;
    }
}
