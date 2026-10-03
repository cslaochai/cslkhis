package com.his.emr.enums;

import lombok.Getter;

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

    public static DeviceMonitorStatusEnum fromCode(int code) {
        for (DeviceMonitorStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        DeviceMonitorStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
