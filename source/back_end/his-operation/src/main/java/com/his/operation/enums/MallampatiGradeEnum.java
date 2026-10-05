package com.his.operation.enums;

import lombok.Getter;

/**
 * Mallampati 气道分级（1-Ⅰ 2-Ⅱ 3-Ⅲ 4-Ⅳ）。
 *
 * <p>气道评估的临床口径，不是给人维护的字典，故不建字典。
 */
@Getter
public enum MallampatiGradeEnum {

    I(1, "Ⅰ级（可见软腭/悬雍垂）"),
    II(2, "Ⅱ级（可见软腭/咽峡弓）"),
    III(3, "Ⅲ级（仅见软腭）"),
    IV(4, "Ⅳ级（仅见硬腭）");

    private final int code;
    private final String label;

    MallampatiGradeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MallampatiGradeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MallampatiGradeEnum e : values()) {
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
        MallampatiGradeEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 渲染「未知」，脏值渲染「未知(n)」保留原始码值；
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        MallampatiGradeEnum e = code == null ? null : fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
