package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 输液座位状态枚举
 */
@Getter
public enum InfusionSeatStatusEnum {

    FREE(1, "空闲"),
    OCCUPIED(2, "占用"),
    DISABLED(3, "停用");

    private final int code;
    private final String label;

    InfusionSeatStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfusionSeatStatusEnum fromCode(int code) {
        for (InfusionSeatStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InfusionSeatStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
