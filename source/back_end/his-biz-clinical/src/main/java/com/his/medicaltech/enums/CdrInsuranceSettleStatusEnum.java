package com.his.medicaltech.enums;

public enum CdrInsuranceSettleStatusEnum {
    PENDING(1, "待结算"),
    SETTLED(2, "已结算"),
    UPLOADED(3, "已上传"),
    AUDITED(4, "已审核");

    private final int code;
    private final String label;

    CdrInsuranceSettleStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrInsuranceSettleStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrInsuranceSettleStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrInsuranceSettleStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrInsuranceSettleStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
