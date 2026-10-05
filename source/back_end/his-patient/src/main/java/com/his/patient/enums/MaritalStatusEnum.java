package com.his.patient.enums;

import lombok.Getter;

/**
 * 婚姻状况枚举（1-未婚 2-已婚 3-离异 4-丧偶）
 */
@Getter
public enum MaritalStatusEnum {

    UNMARRIED(1, "未婚"),
    MARRIED(2, "已婚"),
    DIVORCED(3, "离异"),
    WIDOWED(4, "丧偶");

    private final int code;
    private final String label;

    MaritalStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MaritalStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MaritalStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "";
        }
        MaritalStatusEnum item = fromCode(code);
        return item != null ? item.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        MaritalStatusEnum item = fromCode(code);
        return item != null ? item.label : "未知(" + code + ")";
    }
}
