package com.his.system.enums;

import lombok.Getter;

@Getter
public enum ChannelEnum {

    SYSTEM(1, "system", "站内信"),
    SMS(2, "sms", "短信"),
    WECHAT(3, "wechat", "微信"),
    EMAIL(4, "email", "邮件");

    private final Integer code;
    private final String channel;
    private final String desc;

    ChannelEnum(Integer code, String channel, String desc) {
        this.code = code;
        this.channel = channel;
        this.desc = desc;
    }

    public static ChannelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChannelEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static ChannelEnum getByChannel(String channel) {
        if (channel == null) {
            return null;
        }
        for (ChannelEnum e : values()) {
            if (e.getChannel().equals(channel)) {
                return e;
            }
        }
        return null;
    }
}