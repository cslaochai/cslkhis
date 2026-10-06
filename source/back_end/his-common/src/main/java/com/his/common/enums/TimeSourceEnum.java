package com.his.common.enums;

/**
 * 签名时刻的时间来源。
 *
 * <p><b>这一个枚举存在的唯一理由是「不许谎报」</b>：
 * 本机时钟可以随手改，第三方 TSA 才有法律意义上的"可信时间"。
 * 本期没有接入 TSA，所以全部签名都是 {@link #LOCAL}，
 * 页面必须如实显示"本机时钟"，而不是含糊地写"已加时间戳"。
 *
 * <p>{@link #TSA} 与 {@link #HOSPITAL_NTP} 是留给后续接入的位置，
 * <b>没有对应实现就绝不能写这两个值</b>（写入前会校验 {@code tsa_serial} 非空）。
 */
public enum TimeSourceEnum {

    LOCAL(1, "本机时钟"),
    HOSPITAL_NTP(2, "院内授时服务器"),
    TSA(3, "第三方可信时间戳");

    private final int code;
    private final String text;

    TimeSourceEnum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public static TimeSourceEnum parse(Integer code) {
        if (code == null) {
            return null;
        }
        for (TimeSourceEnum t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        return null;
    }

    public static String textOf(Integer code) {
        TimeSourceEnum t = parse(code);
        if (t != null) {
            return t.text;
        }
        return code == null ? "—" : "未知(" + code + ")";
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    /**
     * 是否属于"可信时间"（可对外声称具备时间戳效力）
     */
    public boolean trusted() {
        return this == HOSPITAL_NTP || this == TSA;
    }
}
