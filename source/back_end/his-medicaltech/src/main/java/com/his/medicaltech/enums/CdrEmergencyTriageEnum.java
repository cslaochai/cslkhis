package com.his.medicaltech.enums;

public enum CdrEmergencyTriageEnum {
    LEVEL_1(1, "I 级（濒危）"),
    LEVEL_2(2, "II 级（危重）"),
    LEVEL_3(3, "III 级（急症）"),
    LEVEL_4(4, "IV 级（非急症）");

    private final int code;
    private final String label;

    CdrEmergencyTriageEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrEmergencyTriageEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrEmergencyTriageEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrEmergencyTriageEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrEmergencyTriageEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
