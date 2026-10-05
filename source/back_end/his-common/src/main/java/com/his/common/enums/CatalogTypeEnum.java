package com.his.common.enums;

import lombok.Getter;

/**
 * 医保目录类别枚举
 */
@Getter
public enum CatalogTypeEnum {

    SELF_PAY(0, "自费"),
    CLASS_A(1, "甲类"),
    CLASS_B(2, "乙类"),
    CLASS_C(3, "丙类");

    private final int code;
    private final String label;

    CatalogTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CatalogTypeEnum fromCode(int code) {
        for (CatalogTypeEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        CatalogTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        CatalogTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
