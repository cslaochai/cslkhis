package com.his.common.enums;

/**
 * 证书签发方式。
 */
public enum CertIssuedModeEnum {

    MANUAL(1, "人工签发"),
    AUTO(2, "系统自动签发");

    private final int code;
    private final String text;

    CertIssuedModeEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static CertIssuedModeEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (CertIssuedModeEnum m : values()) {
            if (m.code == code) {
                return m;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        CertIssuedModeEnum m = parse(code);
        if (m != null) {
            return m.text;
        }
        return code == null ? "—" : "未知(" + code + ")";
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
