package com.his.common.enums;

/**
 * 签名时刻的时间来源。
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
