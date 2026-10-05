package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：病案归档状态文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrArchiveStatusEnum {

    PENDING(1, "待归档"),
    ARCHIVED(2, "已归档"),
    SEALED(3, "已封存");

    private final int code;
    private final String label;

    CdrArchiveStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrArchiveStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrArchiveStatusEnum e : values()) {
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
        CdrArchiveStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        return code == null ? "未知" : "未知(" + code + ")";
    }
}
