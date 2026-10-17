package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrQueueStatusEnum {
    WAITING(2, "候诊中"),
    IN_CONSULT(3, "就诊中"),
    SEEN(4, "已就诊"),
    CANCELLED(5, "已退号"),
    MISSED(6, "已过号");

    private final int code;
    private final String label;

    CdrQueueStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrQueueStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrQueueStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrQueueStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrQueueStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrQueueStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
