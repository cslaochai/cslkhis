package com.his.medicaltech.enums;

public enum CdrDischargeStatusEnum {
    NORMAL(1, "正常出院"),
    TRANSFER(2, "转科"),
    SELF_DISCHARGE(3, "自动出院");

    private final int code;
    private final String label;

    CdrDischargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrDischargeStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrDischargeStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrDischargeStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrDischargeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
