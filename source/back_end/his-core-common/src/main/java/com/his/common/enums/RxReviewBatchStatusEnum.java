package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 处方点评批次状态枚举
 */
@Getter
public enum RxReviewBatchStatusEnum {

    RUNNING(1, "进行中"),
    DONE(2, "已完成");

    private final int code;
    private final String label;

    RxReviewBatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RxReviewBatchStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RxReviewBatchStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        RxReviewBatchStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        RxReviewBatchStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RxReviewBatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
