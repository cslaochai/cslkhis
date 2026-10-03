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
}
