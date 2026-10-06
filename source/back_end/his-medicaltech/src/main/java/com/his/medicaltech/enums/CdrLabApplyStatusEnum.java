package com.his.medicaltech.enums;

public enum CdrLabApplyStatusEnum {
    SUBMITTED(1, "已提交"),
    PAID(2, "已缴费"),
    SAMPLED(3, "已采样"),
    TESTING(4, "检验中"),
    REPORTED(5, "已出报告"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    CdrLabApplyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrLabApplyStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrLabApplyStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrLabApplyStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrLabApplyStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
