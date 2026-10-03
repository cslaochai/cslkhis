package com.his.patient.enums;

import lombok.Getter;

/**
 * 住院医嘱状态枚举
 */
@Getter
public enum InpatientOrderStatusEnum {

    PENDING_VERIFY(1, "待校对"),
    VERIFIED(2, "已校对"),
    EXECUTING(3, "执行中"),
    FINISHED(4, "已完成"),
    STOPPED(5, "已停止"),
    CANCELLED(6, "已作废"),
    RETURNED(7, "已退回");

    private final int code;
    private final String label;

    InpatientOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static InpatientOrderStatusEnum fromCode(int code) {
        for (InpatientOrderStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        InpatientOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
