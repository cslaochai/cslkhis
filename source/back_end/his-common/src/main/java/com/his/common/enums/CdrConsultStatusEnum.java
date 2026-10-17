package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrConsultStatusEnum {
    PENDING(0, "待应答"),
    FINISHED(1, "已完成"),
    CANCELLED(2, "已取消"),
    IN_PROGRESS(3, "会诊中");

    private final int code;
    private final String label;

    CdrConsultStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrConsultStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrConsultStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrConsultStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrConsultStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrConsultStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
