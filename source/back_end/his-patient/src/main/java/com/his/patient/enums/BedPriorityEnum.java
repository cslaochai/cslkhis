package com.his.patient.enums;

import lombok.Getter;

/**
 * 床位等待优先级枚举（1-普通 2-急 3-危重）
 */
@Getter
public enum BedPriorityEnum {

    NORMAL(1, "普通"),
    URGENT(2, "急"),
    CRITICAL(3, "危重");

    private final int code;
    private final String label;

    BedPriorityEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static BedPriorityEnum fromCode(int code) {
        for (BedPriorityEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        BedPriorityEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
