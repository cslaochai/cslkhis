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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return code != null && fromCode(code) != null;
    }

    /**
     * 展示用：null 或脏值返回空串（不把「未知」渲染给用户看）。
     */
    public static String getText(Integer code) {
        SurveyChannelEnum item = code == null ? null : fromCode(code);
        return item == null ? "" : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        SurveyChannelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
