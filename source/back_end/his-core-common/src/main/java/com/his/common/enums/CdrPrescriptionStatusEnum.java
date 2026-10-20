package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrPrescriptionStatusEnum {
    DRAFT(1, "草稿"),
    SUBMITTED(2, "已提交"),
    AUDITED(3, "已审核"),
    DISPENSED(4, "已发药"),
    CANCELLED(5, "已取消"),
    RETURNED(6, "已退药");

    private final int code;
    private final String label;

    CdrPrescriptionStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrPrescriptionStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrPrescriptionStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrPrescriptionStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrPrescriptionStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrPrescriptionStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
