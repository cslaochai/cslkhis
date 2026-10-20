package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrPublicHealthReportStatusEnum {
    PENDING(1, "待审核"),
    APPROVED(2, "审核通过"),
    REJECTED(3, "审核驳回");

    private final int code;
    private final String label;

    CdrPublicHealthReportStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrPublicHealthReportStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrPublicHealthReportStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrPublicHealthReportStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrPublicHealthReportStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrPublicHealthReportStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
