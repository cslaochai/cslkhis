package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 药品盘点单状态枚举（码值口径 = 盘点单状态字段的列注释）。
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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static StocktakeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StocktakeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StocktakeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StocktakeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StocktakeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
