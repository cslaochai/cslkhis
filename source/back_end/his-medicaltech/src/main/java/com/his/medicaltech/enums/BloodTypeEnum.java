package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * ABO 血型枚举
 */
@Getter
public enum BloodTypeEnum {

    A(1, "A"),
    B(2, "B"),
    O(3, "O"),
    AB(4, "AB");

    private final int code;
    private final String label;

    BloodTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BloodTypeEnum fromCode(int code) {
        for (BloodTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        BloodTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
