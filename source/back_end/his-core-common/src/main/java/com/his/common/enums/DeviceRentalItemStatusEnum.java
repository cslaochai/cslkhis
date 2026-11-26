package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备共享调配明细状态。
 */
@Getter
public enum DeviceRentalItemStatusEnum {

    PENDING_ISSUE(1, "待发放"),
    IN_USE(2, "使用中"),
    PENDING_ACCEPT(3, "待验收"),
    RETURNED(4, "已归还"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    DeviceRentalItemStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeviceRentalItemStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeviceRentalItemStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        DeviceRentalItemStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeviceRentalItemStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeviceRentalItemStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
