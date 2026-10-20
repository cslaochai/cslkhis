package com.his.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 签名时刻的时间来源。
 */
public enum TimeSourceEnum {

    LOCAL(1, "本机时钟"),
    HOSPITAL_NTP(2, "院内授时服务器"),
    TSA(3, "第三方可信时间戳");

    private final int code;
    private final String label;

    TimeSourceEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 是否属于"可信时间"（可对外声称具备时间戳效力）
     */
    public boolean trusted() {
        return this == HOSPITAL_NTP || this == TSA;
    }

    public static TimeSourceEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (TimeSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    public static String getLabel(Integer code) {
        if (null == code) {
            return "—";
        }
        TimeSourceEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        TimeSourceEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
}
