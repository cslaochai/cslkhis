package com.his.operation.enums;

import lombok.Getter;

/**
 * 器械清点阶段（0-未开始 1-术前清点完成 2-关体前清点完成 3-关体后清点完成）。
 */
@Getter
public enum CountPhaseEnum {

    NONE(0, "未开始"),
    BEFORE(1, "术前清点完成"),
    CLOSURE(2, "关体前清点完成"),
    FINAL(3, "关体后清点完成");

    private final int code;
    private final String label;

    CountPhaseEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CountPhaseEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CountPhaseEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    /** 码值是否合法（写入侧校验用；null 不合法） */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 码值 → 展示文案（本枚举文案唯一出口）。
     *
     * <p><b>本枚举声明的缺省展示文案是「—」</b>：null（未填写）渲染为「—」；
     * 合法码值取 label；脏值（不在枚举内的越界码值）返回空串 {@code ""}，
     * 绝不回落合法文案，也绝不返回 null。
     * 异常 / 审计场景需保留原始码值时用 {@link #labelOrUnknown(Integer)}。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        CountPhaseEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 渲染「未知」，脏值渲染「未知(n)」保留原始码值；
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        CountPhaseEnum e = code == null ? null : fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
