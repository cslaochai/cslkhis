package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 发药状态枚举
 */
@Getter
public enum DispensingStatus {

    PENDING(1, "待发药"),
    DISPENSED(2, "已发药"),
    PICKED_UP(3, "已取药"),
    RETURNED(4, "已退药");

    private final int code;
    private final String label;

    DispensingStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispensingStatus fromCode(int code) {
        for (DispensingStatus status : values()) {
            if (status.code == code) return status;
        }
        return null;
    }
}
