package com.his.operation.enums;

import lombok.Getter;

/**
 * 切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）。
 *
 * <p>注意是 <b>0~3</b> 而不是 1~4 —— 0 类（如经自然腔道）是合法值，
 * 用"非空即合法"或"1~3"去校验会把 0 类手术判成非法。
 */
@Getter
public enum OperationIncisionEnum {

    CLASS_0(0, "0类"),
    CLASS_I(1, "Ⅰ类"),
    CLASS_II(2, "Ⅱ类"),
    CLASS_III(3, "Ⅲ类");

    private final int code;
    private final String label;

    OperationIncisionEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static OperationIncisionEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationIncisionEnum e : values()) {
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
     * 绝不回落合法文案（切口等级回落成"Ⅰ类"等于把没核实的事记成核实了），也绝不返回 null。
     * 异常 / 审计场景需保留原始码值时用 {@link #labelOrUnknown(Integer)}。
     */
    public static String getText(Integer code) {
        if (code == null) {
            return "—";
        }
        OperationIncisionEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 渲染「未知」，脏值渲染「未知(n)」保留原始码值；
     * 绝不用于前端展示。
     */
    public static String labelOrUnknown(Integer code) {
        OperationIncisionEnum e = code == null ? null : fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }
}
