package com.his.patient.enums;

import lombok.Getter;

/**
 * VTE发生时机枚举
 */
@Getter
public enum VteOnsetEnum {

    IN_HOSPITAL(1, "院内发生"),
    PRE_EXISTING(2, "入院时已存在");

    private final int code;
    private final String label;

    VteOnsetEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VteOnsetEnum fromCode(int code) {
        for (VteOnsetEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String labelOf(Integer code) {
        VteOnsetEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
