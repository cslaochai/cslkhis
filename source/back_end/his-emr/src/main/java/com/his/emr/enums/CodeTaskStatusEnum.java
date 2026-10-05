package com.his.emr.enums;

import lombok.Getter;

/**
 * 病案编码任务状态枚举
 */
@Getter
public enum CodeTaskStatusEnum {

    PENDING(1, "待编码"),
    SUBMITTED(2, "已提交"),
    DONE(3, "已完成"),
    REWORK(4, "已退修");

    private final int code;
    private final String label;

    CodeTaskStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CodeTaskStatusEnum fromCode(int code) {
        for (CodeTaskStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        CodeTaskStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
