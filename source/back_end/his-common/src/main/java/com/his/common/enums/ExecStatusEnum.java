package com.his.common.enums;

import lombok.Getter;

/**
 * 执行状态枚举
 */
@Getter
public enum ExecStatusEnum {

    PENDING(1, "待执行"),
    EXECUTED(2, "已执行"),
    SKIPPED(3, "已跳过"),
    RETURNED(4, "已退回");

    private final int code;
    private final String label;

    ExecStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ExecStatusEnum fromCode(int code) {
        for (ExecStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        ExecStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        ExecStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
