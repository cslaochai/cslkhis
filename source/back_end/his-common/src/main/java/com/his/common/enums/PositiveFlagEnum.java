package com.his.common.enums;

import lombok.Getter;

/**
 * 报告阴阳性（biz_report.positive_flag，字典 his_positive_flag）
 */
@Getter
public enum PositiveFlagEnum {

    UNJUDGED(0, "未判定"),
    NEGATIVE(1, "阴性"),
    POSITIVE(2, "阳性"),
    NO_ABNORMALITY(3, "未见异常");

    private final int code;
    private final String label;

    PositiveFlagEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PositiveFlagEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PositiveFlagEnum item : values()) {
            if (item.code == code) return item;
        }
        return null;
    }

    /** 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。 */
    public static String labelOf(Integer code) {
        PositiveFlagEnum item = fromCode(code);
        return item == null ? null : item.label;
    }
}
