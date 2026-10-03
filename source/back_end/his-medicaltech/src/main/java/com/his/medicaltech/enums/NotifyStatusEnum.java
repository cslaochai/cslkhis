package com.his.medicaltech.enums;

/**
 * 通知状态枚举
 */
public enum NotifyStatusEnum {

    NONE(0, "未通知"),
    SENT(1, "已通知");

    private final int code;
    private final String description;

    NotifyStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NotifyStatusEnum getByCode(int code) {
        for (NotifyStatusEnum status : NotifyStatusEnum.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
