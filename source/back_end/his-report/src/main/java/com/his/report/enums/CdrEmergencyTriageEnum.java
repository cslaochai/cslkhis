package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：急诊分诊级别文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrEmergencyTriageEnum {

    LEVEL_1(1, "I 级（濒危）"),
    LEVEL_2(2, "II 级（危重）"),
    LEVEL_3(3, "III 级（急症）"),
    LEVEL_4(4, "IV 级（非急症）");

    private final int code;
    private final String label;

    CdrEmergencyTriageEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrEmergencyTriageEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrEmergencyTriageEnum e : values()) {
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
        CdrEmergencyTriageEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        CdrEmergencyTriageEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
