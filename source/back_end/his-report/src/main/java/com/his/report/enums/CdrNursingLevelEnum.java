package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：护理级别文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrNursingLevelEnum {

    SPECIAL(1, "特级护理"),
    LEVEL_1(2, "一级护理"),
    LEVEL_2(3, "二级护理"),
    LEVEL_3(4, "三级护理");

    private final int code;
    private final String label;

    CdrNursingLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrNursingLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrNursingLevelEnum e : values()) {
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
        CdrNursingLevelEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
