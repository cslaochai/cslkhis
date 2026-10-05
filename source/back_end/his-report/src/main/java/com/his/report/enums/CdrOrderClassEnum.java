package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：住院医嘱类别文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrOrderClassEnum {

    DRUG(1, "药品"),
    EXAM(2, "检查"),
    LAB(3, "检验"),
    TREATMENT(4, "治疗"),
    NURSING(5, "护理"),
    OPERATION(6, "手术"),
    TRANSFUSION(7, "输血"),
    MONITOR(8, "监护"),
    OTHER(9, "其他"),
    NUTRITION(10, "临床营养");

    private final int code;
    private final String label;

    CdrOrderClassEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrOrderClassEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrOrderClassEnum e : values()) {
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

    /** 展示用：null / 脏码值一律返回空串，绝不返回 null、绝不回落合法值 */
    public static String getText(Integer code) {
        CdrOrderClassEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
