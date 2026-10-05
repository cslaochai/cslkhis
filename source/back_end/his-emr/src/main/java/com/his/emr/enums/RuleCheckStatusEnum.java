package com.his.emr.enums;

import lombok.Getter;

/**
 * 临床规则核查处理状态枚举
 */
@Getter
public enum RuleCheckStatusEnum {

    PENDING(1, "待处理"),
    HANDLED(2, "已处理"),
    IGNORED(3, "已忽略");

    private final int code;
    private final String label;

    RuleCheckStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static RuleCheckStatusEnum fromCode(int code) {
        for (RuleCheckStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        RuleCheckStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
