package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * ICU 留室状态枚举
 */
@Getter
public enum IcuStayStatusEnum {

    IN(1, "在科"),
    OUT(2, "已出科");

    private final int code;
    private final String label;

    IcuStayStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static IcuStayStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (IcuStayStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        IcuStayStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        IcuStayStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        IcuStayStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
