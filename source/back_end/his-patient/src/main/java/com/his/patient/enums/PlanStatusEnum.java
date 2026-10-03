package com.his.patient.enums;

import lombok.Getter;

/**
 * 膳食方案状态枚举
 */
@Getter
public enum PlanStatusEnum {

    RUNNING(1, "执行中"),
    STOPPED(2, "已停止"),
    CANCELED(3, "已作废");

    private final int code;
    private final String label;

    PlanStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PlanStatusEnum fromCode(int code) {
        for (PlanStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        PlanStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
