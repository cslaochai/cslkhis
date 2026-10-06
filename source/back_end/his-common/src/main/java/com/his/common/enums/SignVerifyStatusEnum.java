package com.his.common.enums;

/**
 * 最近一次验签的结果。
 *
 * <p>0-未校验是一个**真实且常见**的状态（签完从没验过），
 * 不允许被读成"验签通过"，也不允许被读成"失败"。三态各有各的处置动作。
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
