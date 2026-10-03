package com.his.common.enums;

import lombok.Getter;

/**
 * 随访记录状态枚举
 */
@Getter
public enum FollowupStatusEnum {

    DRAFT(0, "草稿"),
    DONE(1, "已完成");

    private final int code;
    private final String label;

    FollowupStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FollowupStatusEnum fromCode(int code) {
        for (FollowupStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        FollowupStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
