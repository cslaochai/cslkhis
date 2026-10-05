package com.his.patient.enums;

import lombok.Getter;

/**
 * 患者类型（参保性质）枚举
 */
@Getter
public enum PatientTypeEnum {

    SELF_PAY(1, "自费"),
    URBAN_EMPLOYEE(2, "城镇职工医保"),
    URBAN_RESIDENT(3, "城乡居民医保"),
    PUBLIC(4, "公费"),
    OTHER(5, "其他");

    private final int code;
    private final String label;

    PatientTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PatientTypeEnum fromCode(int code) {
        for (PatientTypeEnum item : values()) {
            if (item.code == code) return item;
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
        PatientTypeEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        PatientTypeEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
