package com.his.medicaltech.enums;

public enum CdrDiagTypeEnum {
    MAIN(1, "主要诊断"),
    OTHER(2, "其他诊断");

    private final int code;
    private final String label;

    CdrDiagTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrDiagTypeEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrDiagTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrDiagTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrDiagTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
