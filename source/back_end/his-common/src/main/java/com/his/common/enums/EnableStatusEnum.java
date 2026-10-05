package com.his.common.enums;

import lombok.Getter;

/**
 * 启用状态枚举
 */
@Getter
public enum EnableStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String label;

    EnableStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static EnableStatusEnum fromCode(int code) {
        for (EnableStatusEnum status : values()) {
            if (status.code == code) return status;
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
        EnableStatusEnum status = fromCode(code);
        return status != null ? status.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        EnableStatusEnum status = fromCode(code);
        return status != null ? status.label : "未知(" + code + ")";
    }
}
