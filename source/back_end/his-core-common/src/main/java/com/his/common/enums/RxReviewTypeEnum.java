package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方点评类型枚举
 */
@Getter
public enum RxReviewTypeEnum {

    ROUTINE(1, "常规点评"),
    SPECIAL(2, "专项点评");

    private final int code;
    private final String label;

    RxReviewTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RxReviewTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RxReviewTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RxReviewTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RxReviewTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
