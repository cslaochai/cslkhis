package com.his.emr.enums;

public enum QcStatusEnum {
    PENDING(1, "待处理"),
    PROCESSED(2, "已处理"),
    IGNORED(3, "已忽略");

    private final int code;
    private final String label;

    QcStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() { return code; }
    public String getLabel() { return label; }

    public static QcStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (QcStatusEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    public static String getText(Integer code) {
        QcStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    public static String labelOrUnknown(Integer code) {
        QcStatusEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.label;
    }
}
