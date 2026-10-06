package com.his.common.enums;

/**
 * 被签对象（病历/医嘱）行上的签名锚点状态。
 */
public enum ObjectSignStatusEnum {

    UNSIGNED(0, "未签名"),
    SIGNED(1, "已签名"),
    INVALIDATED(2, "签名已失效");

    private final int code;
    private final String text;

    ObjectSignStatusEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static ObjectSignStatusEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (ObjectSignStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        ObjectSignStatusEnum s = parse(code);
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
