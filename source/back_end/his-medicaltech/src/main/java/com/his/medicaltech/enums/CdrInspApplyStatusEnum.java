package com.his.medicaltech.enums;

public enum CdrInspApplyStatusEnum {
    SUBMITTED(1, "已提交"),
    PAID(2, "已缴费"),
    BOOKED(3, "已预约"),
    EXAMINING(4, "检查中"),
    REPORTED(5, "已出报告"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    CdrInspApplyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrInspApplyStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrInspApplyStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrInspApplyStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrInspApplyStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
