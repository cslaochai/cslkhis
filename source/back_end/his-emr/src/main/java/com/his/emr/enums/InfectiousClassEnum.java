package com.his.emr.enums;

import lombok.Getter;

/**
 * 传染病类别枚举
 */
@Getter
public enum InfectiousClassEnum {

    CLASS_A(1, "甲类"),
    CLASS_B(2, "乙类"),
    CLASS_C(3, "丙类");

    private final int code;
    private final String label;

    InfectiousClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InfectiousClassEnum fromCode(int code) {
        for (InfectiousClassEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案。
     */
    public static String labelOf(Integer code) {
        InfectiousClassEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
