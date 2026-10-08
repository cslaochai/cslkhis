package com.his.common.enums;

import lombok.Getter;

/**
 * 手术/操作级别枚举（1~4 级，四级风险最高）。
 */
@Getter
public enum TechLevelEnum {

    LEVEL_1(1, "一级"),
    LEVEL_2(2, "二级"),
    LEVEL_3(3, "三级"),
    LEVEL_4(4, "四级");

    private final int code;
    private final String label;

    TechLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static TechLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TechLevelEnum level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return null;
    }

    /**
     * 未知码值渲染成「未知(码值)」，绝不回落成某个合法级别
     */
    public static String getText(Integer code) {
        TechLevelEnum level = fromCode(code);
        return level == null ? "未知(" + code + ")" : level.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        TechLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}
