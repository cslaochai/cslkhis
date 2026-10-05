package com.his.common.enums;

import lombok.Getter;

/**
 * 校验结果枚举
 */
@Getter
public enum CheckResultEnum {

    FAIL(0, "不通过"),
    PASS(1, "通过");

    private final int code;
    private final String label;

    CheckResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CheckResultEnum fromCode(int code) {
        for (CheckResultEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String labelOf(Integer code) {
        CheckResultEnum item = code == null ? null : fromCode(code);
        return item == null ? null : item.label;
    }
}
