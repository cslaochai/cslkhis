package com.his.common.enums;

import lombok.Getter;

/**
 * 统一性别枚举（字典性别字典：1-男 2-女 9-未知）
 */
@Getter
public enum SysGenderEnum {

    MALE(1, "男"),
    FEMALE(2, "女"),
    UNKNOWN(9, "未知");

    private final int code;
    private final String label;

    SysGenderEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static SysGenderEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SysGenderEnum g : values()) {
            if (g.code == code) {
                return g;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值→展示文案（全系统性别文案唯一出口）。
     *
     * <p>语义：null 与未填写一律渲染为「未知」；合法码值取 label；脏值（越界码值）暴露原值「未知(n)」，
     * 绝不悄悄回落到某个合法性别 —— 把看不懂的码值说成「女」比没有文案更危险。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "未知";
        }
        SysGenderEnum g = fromCode(code);
        return g != null ? g.label : "未知(" + code + ")";
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        SysGenderEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
