package com.his.emr.enums;

import lombok.Getter;

/**
 * 处方流转状态枚举
 */
@Getter
public enum RxFlowStatusEnum {

    FLOWED(1, "已流转"),
    TAKEN(2, "已取药"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    RxFlowStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxFlowStatusEnum fromCode(int code) {
        for (RxFlowStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        RxFlowStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
