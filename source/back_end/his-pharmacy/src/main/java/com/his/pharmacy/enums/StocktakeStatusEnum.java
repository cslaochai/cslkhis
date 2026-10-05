package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 药品盘点单状态枚举（码值口径 = biz_stocktake.status 列注释）。
 *
 * <p>文案供后端拼提示用，页面渲染仍走字典。
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

    public static String labelOf(Integer code) {
        StocktakeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
