package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 配送任务优先级：急送插队，常规按 FIFO，批量补货最后。
 */
@Getter
public enum RobotDispatchPriorityEnum {

    NORMAL(1, "常规"),
    BATCH(2, "批量补货"),
    URGENT(3, "急送");

    private final int code;
    private final String label;

    RobotDispatchPriorityEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RobotDispatchPriorityEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RobotDispatchPriorityEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        RobotDispatchPriorityEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RobotDispatchPriorityEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
