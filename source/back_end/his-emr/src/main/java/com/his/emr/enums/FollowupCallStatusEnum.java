package com.his.emr.enums;

import lombok.Getter;

/**
 * 随访电话外呼状态枚举
 */
@Getter
public enum FollowupCallStatusEnum {

    NONE(0, "未外呼"),
    WAITING(1, "待外呼"),
    CONNECTED(2, "已接通"),
    NO_ANSWER(3, "未接通");

    private final int code;
    private final String label;

    FollowupCallStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FollowupCallStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FollowupCallStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        FollowupCallStatusEnum item = fromCode(code);
        return item == null ? null : item.label;
    }
}
