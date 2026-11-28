package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 物流机器人配送任务单状态。
 */
@Getter
public enum RobotDispatchStatusEnum {

    PENDING_DISPATCH(1, "待调度"),
    DISPATCHED(2, "已派单"),
    DELIVERING(3, "配送中"),
    COMPLETED(4, "已签收"),
    CANCELLED(5, "已取消"),
    ABNORMAL(6, "异常");

    private final int code;
    private final String label;

    RobotDispatchStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RobotDispatchStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (RobotDispatchStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        RobotDispatchStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        RobotDispatchStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
