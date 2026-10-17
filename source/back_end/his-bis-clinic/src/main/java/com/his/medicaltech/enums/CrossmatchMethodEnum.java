package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * 交叉配血方法枚举
 */
@Getter
public enum CrossmatchMethodEnum {

    SALINE(1, "盐水法"),
    POLYBRENE(2, "凝聚胺法"),
    COOMBS(3, "抗人球蛋白法"),
    MICRO_COLUMN_GEL(4, "微柱凝胶法");

    private final int code;
    private final String label;

    CrossmatchMethodEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CrossmatchMethodEnum fromCode(int code) {
        for (CrossmatchMethodEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用：null 或不在枚举内返回空串（不把「未知」渲染给用户看）
     */
    public static String getText(Integer code) {
        CrossmatchMethodEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        CrossmatchMethodEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
