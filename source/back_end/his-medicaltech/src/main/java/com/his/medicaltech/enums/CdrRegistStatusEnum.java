package com.his.medicaltech.enums;

public enum CdrRegistStatusEnum {
    REGISTERED(1, "已挂号"),
    SIGNED_IN(2, "已签到"),
    SEEN(3, "已接诊"),
    VISITED(4, "已就诊"),
    CANCELLED(5, "已退号"),
    MISSED(6, "已过号");

    private final int code;
    private final String label;

    CdrRegistStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrRegistStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrRegistStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrRegistStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrRegistStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
