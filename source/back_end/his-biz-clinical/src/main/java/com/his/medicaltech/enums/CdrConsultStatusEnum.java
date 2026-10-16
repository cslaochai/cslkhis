package com.his.medicaltech.enums;

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
        if (code == null) return null;
        for (CdrConsultStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrConsultStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrConsultStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
