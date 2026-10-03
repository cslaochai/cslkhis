package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 危急值类型枚举
 */
@Getter
public enum CriticalTypeEnum {

    LOW(1, "偏低"),
    HIGH(2, "偏高");

    private final int code;
    private final String label;

    CriticalTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CriticalTypeEnum fromCode(int code) {
        for (CriticalTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        CriticalTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
