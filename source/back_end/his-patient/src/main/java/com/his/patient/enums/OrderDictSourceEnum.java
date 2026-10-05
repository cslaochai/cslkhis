package com.his.patient.enums;

import lombok.Getter;

/**
 * 医嘱字典来源枚举
 */
@Getter
public enum OrderDictSourceEnum {

    BUILT_IN(1, "系统内置"),
    CUSTOM(2, "自定义");

    private final int code;
    private final String label;

    OrderDictSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderDictSourceEnum fromCode(int code) {
        for (OrderDictSourceEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        OrderDictSourceEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
