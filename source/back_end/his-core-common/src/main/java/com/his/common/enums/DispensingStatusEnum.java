package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 发药状态枚举
 */
@Getter
public enum DispensingStatusEnum {

    PENDING(1, "待发药"),
    DISPENSED(2, "已发药"),
    RETURNED(3, "已退药"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    DispensingStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DispensingStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DispensingStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DispensingStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DispensingStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DispensingStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
