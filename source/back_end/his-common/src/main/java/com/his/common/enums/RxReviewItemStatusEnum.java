package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方点评明细状态枚举
 */
@Getter
public enum RxReviewItemStatusEnum {

    PENDING(0, "待点评"),
    DONE(1, "已点评");

    private final int code;
    private final String label;

    RxReviewItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewItemStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RxReviewItemStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RxReviewItemStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RxReviewItemStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RxReviewItemStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
