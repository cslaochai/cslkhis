package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 通知状态枚举
 */
public enum NotifyStatusEnum {

    NONE(0, "未通知"),
    SENT(1, "已通知");

    private final int code;
    private final String description;

    NotifyStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static NotifyStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (NotifyStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        NotifyStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.description;
    }

    public static String getLabel(Integer code, String defaultValue) {
        NotifyStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.description;
    }

    public static String getLabelOrUnknown(Integer code) {
        NotifyStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.description;
    }
}
