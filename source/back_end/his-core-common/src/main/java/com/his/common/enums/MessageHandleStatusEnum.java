package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 站内信待办的处理状态（NULL 表示通知型消息，用已读状态闭环，不参与本枚举）
 */
@Getter
public enum MessageHandleStatusEnum {

    PENDING(0, "待处理"),
    PROCESSED(1, "已处理"),
    CLOSED(2, "已关闭");

    private final int code;
    private final String label;

    MessageHandleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MessageHandleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (MessageHandleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        MessageHandleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        MessageHandleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        MessageHandleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
