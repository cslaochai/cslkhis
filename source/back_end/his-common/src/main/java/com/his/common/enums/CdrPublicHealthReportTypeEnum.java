package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

public enum CdrPublicHealthReportTypeEnum {
    INFECTIOUS(1, "传染病"),
    DEATH_CAUSE(2, "死因监测"),
    CHRONIC(3, "慢性病"),
    OTHER(4, "其他");

    private final int code;
    private final String label;

    CdrPublicHealthReportTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrPublicHealthReportTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (CdrPublicHealthReportTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        CdrPublicHealthReportTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        CdrPublicHealthReportTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        CdrPublicHealthReportTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
