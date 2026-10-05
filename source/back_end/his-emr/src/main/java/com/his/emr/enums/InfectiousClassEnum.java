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
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        InfectiousClassEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查。
     */
    public static String labelOrUnknown(Integer code) {
        InfectiousClassEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
