package com.his.patient.enums;

import lombok.Getter;

/**
 * 体检记录状态枚举
 */
@Getter
public enum CheckupStatusEnum {

    REGISTERED(1, "已登记"),
    IN_PROGRESS(2, "检查中"),
    FINISHED(3, "已完成"),
    REPORTED(4, "已出报告");

    private final int code;
    private final String label;

    CheckupStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckupStatusEnum fromCode(int code) {
        for (CheckupStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        CheckupStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
