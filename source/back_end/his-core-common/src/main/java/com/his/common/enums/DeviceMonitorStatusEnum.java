package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 导管目标性监测在管状态枚举
 */
@Getter
public enum DeviceMonitorStatusEnum {

    IN_USE(1, "在管"),
    REMOVED(2, "已拔管");

    private final int code;
    private final String label;

    DeviceMonitorStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeviceMonitorStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeviceMonitorStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DeviceMonitorStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeviceMonitorStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeviceMonitorStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
