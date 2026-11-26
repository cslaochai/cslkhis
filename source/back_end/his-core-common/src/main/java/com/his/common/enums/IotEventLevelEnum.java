package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物联网设备事件级别（biz_iot_event.level）
 */
@Getter
public enum IotEventLevelEnum {

    INFO(1, "提示"),
    WARNING(2, "警告"),
    CRITICAL(3, "严重");

    private final int code;
    private final String label;

    IotEventLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IotEventLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IotEventLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        IotEventLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IotEventLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IotEventLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
