package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院请假状态枚举
 */
@Getter
public enum LeaveStatusEnum {

    PENDING(1, "待审批"),
    APPROVED(2, "已批准"),
    LEFT(3, "已离院"),
    RETURNED(4, "已返回"),
    REJECTED(5, "已拒绝"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String label;

    LeaveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static LeaveStatusEnum fromCode(int code) {
        for (LeaveStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        LeaveStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
