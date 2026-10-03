package com.his.emr.enums;

import lombok.Getter;

/**
 * 满意度触达渠道枚举
 */
@Getter
public enum SurveyChannelEnum {

    PHONE(1, "电话代填"),
    SMS(2, "短信"),
    WECHAT(3, "微信"),
    QR(4, "现场扫码");

    private final int code;
    private final String label;

    SurveyChannelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SurveyChannelEnum fromCode(int code) {
        for (SurveyChannelEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        SurveyChannelEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
