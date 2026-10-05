package com.his.patient.enums;

import lombok.Getter;

/**
 * ICU 留室状态枚举
 */
@Getter
public enum IcuStayStatusEnum {

    IN(1, "在科"),
    OUT(2, "已出科");

    private final int code;
    private final String label;

    IcuStayStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IcuStayStatusEnum fromCode(int code) {
        for (IcuStayStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        IcuStayStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
