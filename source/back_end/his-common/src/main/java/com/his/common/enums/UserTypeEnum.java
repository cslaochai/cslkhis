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

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        UserTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        UserTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
