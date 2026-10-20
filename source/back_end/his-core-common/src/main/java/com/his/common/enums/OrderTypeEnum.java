package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 医嘱类型枚举
 */
@Getter
public enum OrderTypeEnum {

    LONG(1, "长期"),
    TEMP(2, "临时");

    private final int code;
    private final String label;

    OrderTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OrderTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (OrderTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        OrderTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        OrderTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        OrderTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
