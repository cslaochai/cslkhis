package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位等待性别限制枚举
 */
@Getter
public enum BedWaitGenderLimitEnum {

    NONE(0, "不限"),
    MALE(1, "限男床"),
    FEMALE(2, "限女床");

    private final int code;
    private final String label;

    BedWaitGenderLimitEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedWaitGenderLimitEnum fromCode(int code) {
        for (BedWaitGenderLimitEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        BedWaitGenderLimitEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
