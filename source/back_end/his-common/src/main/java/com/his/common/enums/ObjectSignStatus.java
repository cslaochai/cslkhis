package com.his.common.enums;

/**
 * 被签对象（病历/医嘱）行上的签名锚点状态。
 *
 * <p>与 {@link SignStatus} 的区别：{@code SignStatus} 说的是"某一条签名记录"的状态，
 * 本枚举说的是"这份病历当前的签名情况"。
 *
 * <p>{@link #INVALIDATED} 必须与 {@link #UNSIGNED} 分开：
 * 「签名被作废」和「从来没签过」是两次不同的事实，
 * 回落成 UNSIGNED 会让"有人作废过签名"这件事从列表上彻底消失。
 */
public enum ObjectSignStatus {

    UNSIGNED(0, "未签名"),
    SIGNED(1, "已签名"),
    INVALIDATED(2, "签名已失效");

    private final int code;
    private final String text;

    ObjectSignStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static ObjectSignStatus parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (ObjectSignStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        ObjectSignStatus s = parse(code);
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
