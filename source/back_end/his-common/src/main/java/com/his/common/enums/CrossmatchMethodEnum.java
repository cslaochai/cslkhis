package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public static CrossmatchMethodEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CrossmatchMethodEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CrossmatchMethodEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CrossmatchMethodEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CrossmatchMethodEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
