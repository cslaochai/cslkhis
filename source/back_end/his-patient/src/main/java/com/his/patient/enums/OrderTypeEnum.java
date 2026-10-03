package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱类型枚举
 */
@Getter
public enum OrderTypeEnum {

    LONG(1, "长期"),
    TEMP(2, "临时");

    private final int code;
    private final String label;

    OrderTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderTypeEnum fromCode(int code) {
        for (OrderTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        OrderTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
