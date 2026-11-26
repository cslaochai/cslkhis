package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 设备借用紧急程度。
 */
@Getter
public enum DeviceRentalUrgencyEnum {

    NORMAL(1, "普通"),
    URGENT(2, "紧急"),
    RESUSCITATION(3, "急救");

    private final int code;
    private final String label;

    DeviceRentalUrgencyEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DeviceRentalUrgencyEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DeviceRentalUrgencyEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        DeviceRentalUrgencyEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DeviceRentalUrgencyEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DeviceRentalUrgencyEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
