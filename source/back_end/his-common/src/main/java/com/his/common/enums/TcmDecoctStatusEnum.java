package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 中药代煎单状态
 */
@Getter
@AllArgsConstructor
public enum TcmDecoctStatusEnum {

    /**
     * 1-待煎（发药完成即生成）
     */
    PENDING(1, "待煎"),

    /**
     * 2-已煎（煎好并按剂封装）
     */
    DECOCTED(2, "已煎"),

    /**
     * 3-已取（患者取走，终态）
     */
    PICKED(3, "已取"),

    /**
     * 9-已作废（终态，必须写原因）
     */
    CANCELLED(9, "已作废");

    private final Integer code;
    private final String label;

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    public static TcmDecoctStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TcmDecoctStatusEnum item : values()) {
            if (code.equals(item.code)) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        TcmDecoctStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TcmDecoctStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        TcmDecoctStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
