package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 盘点明细过账标记枚举（码值口径 = 盘点明细过账标记字段的列注释）。
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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static StocktakePostFlagEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StocktakePostFlagEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StocktakePostFlagEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StocktakePostFlagEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StocktakePostFlagEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
