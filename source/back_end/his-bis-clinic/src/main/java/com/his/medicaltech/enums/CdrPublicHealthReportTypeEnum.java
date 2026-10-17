package com.his.medicaltech.enums;

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
        if (code == null) return null;
        for (CdrPublicHealthReportTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrPublicHealthReportTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrPublicHealthReportTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
