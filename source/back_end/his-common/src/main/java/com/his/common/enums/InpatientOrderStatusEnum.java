package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 住院医嘱状态枚举
 */
@Getter
public enum InpatientOrderStatusEnum {

    PENDING_VERIFY(1, "待校对"),
    VERIFIED(2, "已校对"),
    EXECUTING(3, "执行中"),
    FINISHED(4, "已完成"),
    STOPPED(5, "已停止"),
    CANCELLED(6, "已作废"),
    RETURNED(7, "已退回");

    private final int code;
    private final String label;

    InpatientOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientOrderStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (InpatientOrderStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        InpatientOrderStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        InpatientOrderStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        InpatientOrderStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
