package com.his.patient.enums;

import lombok.Getter;

/**
 * 体检结果异常标志枚举
 */
@Getter
public enum CheckupAbnormalFlagEnum {

    NORMAL(0, "正常"),
    ABNORMAL(1, "异常"),
    TO_CHECK(2, "待查");

    private final int code;
    private final String label;

    CheckupAbnormalFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckupAbnormalFlagEnum fromCode(int code) {
        for (CheckupAbnormalFlagEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        CheckupAbnormalFlagEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
