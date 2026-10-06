package com.his.medicaltech.enums;

public enum CdrEmergencyStatusEnum {
    WAITING(1, "候诊"),
    IN_TREATMENT(2, "诊治中"),
    OBSERVATION(3, "留观"),
    TO_INPATIENT(4, "转住院"),
    LEAVE(5, "离院"),
    DEATH(6, "死亡");

    private final int code;
    private final String label;

    CdrEmergencyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrEmergencyStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrEmergencyStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrEmergencyStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrEmergencyStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
