package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方点评结论枚举
 */
@Getter
public enum RxReviewResultEnum {

    REASONABLE(1, "合理"),
    IRREGULAR(2, "不规范处方"),
    UNSUITABLE(3, "用药不适宜处方"),
    ABNORMAL(4, "超常处方");

    private final int code;
    private final String label;

    RxReviewResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewResultEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RxReviewResultEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RxReviewResultEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RxReviewResultEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RxReviewResultEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
