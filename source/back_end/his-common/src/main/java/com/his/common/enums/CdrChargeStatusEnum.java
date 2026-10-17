package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrChargeStatusEnum {
    PENDING(1, "待收费"),
    PAID(2, "已收费"),
    REFUNDED(3, "已退费"),
    PART_REFUNDED(4, "部分退费"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    CdrChargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrChargeStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrChargeStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrChargeStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrChargeStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrChargeStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
