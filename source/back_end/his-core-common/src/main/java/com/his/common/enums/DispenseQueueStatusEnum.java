package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门诊取药队列状态枚举
 */
@Getter
public enum DispenseQueueStatusEnum {

    WAITING(1, "待取药"),
    DISPENSED(2, "已发药"),
    SKIPPED(3, "已过号"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    DispenseQueueStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispenseQueueStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DispenseQueueStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DispenseQueueStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DispenseQueueStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DispenseQueueStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
