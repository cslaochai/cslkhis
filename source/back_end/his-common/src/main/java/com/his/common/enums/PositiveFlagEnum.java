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

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值不在枚举内（脏数据）返回 null，由调用侧决定兜底文案，不能回落到合法文案。
     */
    public static String getText(Integer code) {
        PositiveFlagEnum item = fromCode(code);
        return item == null ? null : item.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        PositiveFlagEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
