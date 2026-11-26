package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备共享调配申请单状态。
 */
@Getter
public enum DeviceRentalOrderStatusEnum {

    PENDING_APPROVE(1, "待审批"),
    PENDING_ISSUE(2, "待发放"),
    IN_USE(3, "使用中"),
    PENDING_ACCEPT(4, "待验收"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消"),
    REJECTED(7, "已驳回");

    private final int code;
    private final String label;

    DeviceRentalOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeviceRentalOrderStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeviceRentalOrderStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        DeviceRentalOrderStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeviceRentalOrderStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeviceRentalOrderStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
