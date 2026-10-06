package com.his.pharmacy.enums;

import lombok.Getter;

/**
 * 盘点明细过账标记枚举（码值口径 = biz_stocktake_item.posted 列注释）。
 */
@Getter
public enum StocktakePostFlagEnum {

    NONE(0, "未过账"),
    POSTED(1, "已盘盈亏过账"),
    NO_DIFF(2, "无差异免过账");

    private final int code;
    private final String label;

    StocktakePostFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StocktakePostFlagEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StocktakePostFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：null 或脏值返回空串（不把「未知」渲染给用户看） */
    public static String getText(Integer code) {
        StocktakePostFlagEnum item = fromCode(code);
        return item == null ? "" : item.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        StocktakePostFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
