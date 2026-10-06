package com.his.medicaltech.enums;

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
        if (code == null) return null;
        for (CdrPublicHealthReportStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrPublicHealthReportStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrPublicHealthReportStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
