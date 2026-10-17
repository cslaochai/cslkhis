package com.his.common.enums;

import lombok.Getter;

/**
 * 合格性判定结果（1-合格 2-不合格），全仓「合格 / 不合格」这一档判定的唯一枚举。
 */
@Getter
public enum QualifiedResultEnum {

    OK(1, "合格"),
    NG(2, "不合格");

    private final int code;
    private final String label;

    QualifiedResultEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static QualifiedResultEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QualifiedResultEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /**
     * 码值是否合法（入参校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     */
    public static String getText(Integer code) {
        QualifiedResultEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        QualifiedResultEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }

    /**
     * Integer 码值判定：null 安全，语义同 == 比较 int 常量
     */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
