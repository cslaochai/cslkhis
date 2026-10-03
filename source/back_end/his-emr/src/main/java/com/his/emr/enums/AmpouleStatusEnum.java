package com.his.emr.enums;

import lombok.Getter;

/**
 * 空安瓿回收状态枚举
 */
@Getter
public enum AmpouleStatusEnum {

    NA(0, "不适用"),
    PENDING(1, "待回收"),
    RETURNED(2, "已回收");

    private final int code;
    private final String label;

    AmpouleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AmpouleStatusEnum fromCode(int code) {
        for (AmpouleStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        AmpouleStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
