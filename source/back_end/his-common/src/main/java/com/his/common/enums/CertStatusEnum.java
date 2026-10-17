package com.his.common.enums;

/**
 * 员工签名证书状态。
 */
public enum CertStatusEnum {

    ACTIVE(1, "有效"),
    REVOKED(2, "已吊销");

    private final int code;
    private final String text;

    CertStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static CertStatusEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (CertStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        CertStatusEnum s = parse(code);
        if (s != null) {
            return s.text;
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
