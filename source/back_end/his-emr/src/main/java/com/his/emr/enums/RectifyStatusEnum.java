package com.his.emr.enums;

import lombok.Getter;

/**
 * 整改状态枚举
 */
@Getter
public enum RectifyStatusEnum {

    PENDING(1, "待整改"),
    DONE(2, "已整改");

    private final int code;
    private final String label;

    RectifyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RectifyStatusEnum fromCode(int code) {
        for (RectifyStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        RectifyStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
