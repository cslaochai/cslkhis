package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 空安瓿回收状态枚举
 */
@Getter
public enum AmpouleStatusEnum {

    NA(0, "不适用"),
    PENDING(1, "待回收"),
    RETURNED(2, "已回收");

    private final int code;
    private final String label;

    AmpouleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AmpouleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (AmpouleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        AmpouleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        AmpouleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        AmpouleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
