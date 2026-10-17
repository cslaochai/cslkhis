package com.his.medicaltech.enums;

public enum CdrChargeStatusEnum {
    PENDING(1, "待收费"),
    PAID(2, "已收费"),
    REFUNDED(3, "已退费"),
    PART_REFUNDED(4, "部分退费"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    CdrChargeStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static CdrChargeStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (CdrChargeStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        CdrChargeStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        CdrChargeStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
