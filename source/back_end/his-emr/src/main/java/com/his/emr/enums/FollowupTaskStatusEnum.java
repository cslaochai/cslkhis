package com.his.emr.enums;

import lombok.Getter;

/**
 * 随访任务状态枚举
 */
@Getter
public enum FollowupTaskStatusEnum {

    PENDING(1, "待随访"),
    DOING(2, "随访中"),
    DONE(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String label;

    FollowupTaskStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static FollowupTaskStatusEnum fromCode(int code) {
        for (FollowupTaskStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        FollowupTaskStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
