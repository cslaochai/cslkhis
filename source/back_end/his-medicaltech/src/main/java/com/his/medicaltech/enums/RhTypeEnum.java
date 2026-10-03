package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * Rh 血型枚举
 */
@Getter
public enum RhTypeEnum {

    POSITIVE(1, "阳性"),
    NEGATIVE(2, "阴性");

    private final int code;
    private final String label;

    RhTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RhTypeEnum fromCode(int code) {
        for (RhTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        RhTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
