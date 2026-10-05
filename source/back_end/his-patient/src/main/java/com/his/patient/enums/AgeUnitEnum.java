package com.his.patient.enums;

import lombok.Getter;

/**
 * 年龄单位枚举
 */
@Getter
public enum AgeUnitEnum {

    YEAR(1, "岁"),
    MONTH(2, "月"),
    DAY(3, "天");

    private final int code;
    private final String label;

    AgeUnitEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AgeUnitEnum fromCode(int code) {
        for (AgeUnitEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        AgeUnitEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
