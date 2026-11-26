package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物联网设备事件处理状态（biz_iot_event.handled_flag）。
 * <p>
 * 与站内信的 {@link MessageHandleStatusEnum} 不是同一列：设备告警的终态是「已处理/已忽略」，
 * 没有「已关闭」这一档，混用会让告警被标成消息已关闭。
 */
@Getter
public enum IotEventHandleStatusEnum {

    PENDING(0, "未处理"),
    HANDLED(1, "已处理"),
    IGNORED(2, "已忽略");

    private final int code;
    private final String label;

    IotEventHandleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IotEventHandleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IotEventHandleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        IotEventHandleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IotEventHandleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IotEventHandleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
