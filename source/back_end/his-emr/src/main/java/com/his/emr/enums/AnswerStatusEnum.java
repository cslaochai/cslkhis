package com.his.emr.enums;

import lombok.Getter;

/**
 * 满意度答卷状态枚举
 */
@Getter
public enum AnswerStatusEnum {

    VALID(1, "有效"),
    VOID(2, "已作废");

    private final int code;
    private final String label;

    AnswerStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static AnswerStatusEnum fromCode(int code) {
        for (AnswerStatusEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        AnswerStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
