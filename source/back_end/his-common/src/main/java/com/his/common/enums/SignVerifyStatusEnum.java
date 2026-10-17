package com.his.common.enums;

/**
 * 最近一次验签的结果。
 */
public enum SignVerifyStatusEnum {

    UNCHECKED(0, "未校验"),
    PASSED(1, "验签通过"),
    FAILED(2, "验签失败");

    private final int code;
    private final String text;

    SignVerifyStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static SignVerifyStatusEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (SignVerifyStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        SignVerifyStatusEnum s = parse(code);
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
