package com.his.medicaltech.enums;

public enum CdrChargeTypeEnum {
    REGISTRATION(1, "挂号费"),
    DRUG(2, "药品费"),
    EXAM(3, "检查费"),
    LAB(4, "检验费"),
    TREATMENT(5, "治疗费"),
    GENERAL(6, "综合收费");

    private final int code;
    private final String label;

    CdrChargeTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrChargeTypeEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrChargeTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrChargeTypeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrChargeTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
