package com.his.emr.enums;

import lombok.Getter;

/**
 * 发药状态枚举
 */
@Getter
public enum DispensingStatusEnum {

    PENDING(1, "待发药"),
    DISPENSED(2, "已发药"),
    RETURNED(3, "已退药"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    DispensingStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispensingStatusEnum fromCode(int code) {
        for (DispensingStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        DispensingStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
