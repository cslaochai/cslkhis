package com.his.emr.enums;

import lombok.Getter;

/**
 * 随访电话外呼通道枚举
 */
@Getter
public enum FollowupCallChannelEnum {

    MANUAL(1, "人工"),
    AUTO(2, "自动");

    private final int code;
    private final String label;

    FollowupCallChannelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
