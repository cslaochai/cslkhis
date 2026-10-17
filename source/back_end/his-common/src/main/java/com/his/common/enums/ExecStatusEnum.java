package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 执行状态枚举
 */
@Getter
public enum ExecStatusEnum {

    PENDING(1, "待执行"),
    EXECUTED(2, "已执行"),
    SKIPPED(3, "已跳过"),
    RETURNED(4, "已退回");

    private final int code;
    private final String label;

    ExecStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExecStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ExecStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ExecStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ExecStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ExecStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
