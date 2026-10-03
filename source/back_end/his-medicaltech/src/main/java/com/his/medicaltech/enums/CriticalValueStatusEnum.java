package com.his.medicaltech.enums;

/**
 * 危急值状态枚举
 */
public enum CriticalValueStatusEnum {

    PENDING(1, "待接收"),
    RECEIVED(2, "已接收"),
    HANDLED(3, "已处置"),
    CANCELLED(4, "已作废");

    private final int code;
    private final String description;

    CriticalValueStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CriticalValueStatusEnum getByCode(int code) {
        for (CriticalValueStatusEnum status : CriticalValueStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
