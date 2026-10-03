package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 配血单状态枚举
 */
@Getter
public enum CrossmatchOrderStatusEnum {

    PENDING(1, "待配血"),
    MATCHED(2, "已配血"),
    VERIFIED(3, "已复核"),
    VOIDED(4, "已作废");

    private final int code;
    private final String label;

    CrossmatchOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CrossmatchOrderStatusEnum fromCode(int code) {
        for (CrossmatchOrderStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        CrossmatchOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
