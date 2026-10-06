package com.his.medicaltech.enums;

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
        if (code == null) return null;
        for (CdrQueueStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrQueueStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrQueueStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
