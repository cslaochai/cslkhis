package com.his.common.enums;

/**
 * 签名记录自身的状态。
 *
 * <p>只有两个值，且**2 不能删行**：作废的签名仍要能被追到，
 * 否则"这份病历当时是谁签的、后来为什么作废"就没有答案了。
 */
public enum SignStatusEnum {

    VALID(1, "有效"),
    INVALID(2, "已作废");

    private final int code;
    private final String text;

    SignStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static SignStatusEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (SignStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        SignStatusEnum s = parse(code);
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
