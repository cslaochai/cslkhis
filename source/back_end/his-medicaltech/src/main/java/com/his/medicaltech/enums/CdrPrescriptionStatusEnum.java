package com.his.medicaltech.enums;

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
        if (code == null) return null;
        for (CdrPrescriptionStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrPrescriptionStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrPrescriptionStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
