package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 药品盘点单状态枚举（码值口径 = biz_stocktake.status 列注释）。
 */
@Getter
public enum StocktakeStatusEnum {

    COUNTING(1, "盘点中"),
    AUDITING(2, "待复核"),
    POSTED(3, "已过账"),
    CLOSED(4, "已关单");

    private final int code;
    private final String label;

    StocktakeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StocktakeStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StocktakeStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案。null 或不在枚举内（脏数据）返回空串 ""，绝不返回 null、不回落合法文案。
     */
    public static String getText(Integer code) {
        StocktakeStatusEnum e = fromCode(code);
        return e != null ? e.label : "";
    }

    /**
     * 码值→异常/审计文案。null 或不在枚举内返回「未知(n)」，保留原始码值以便排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        if (code == null) {
            return "未知";
        }
        StocktakeStatusEnum e = fromCode(code);
        return e != null ? e.label : "未知(" + code + ")";
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
