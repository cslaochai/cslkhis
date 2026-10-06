package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：住院医嘱状态文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrOrderStatusEnum {

    UNCHECKED(1, "待校对"),
    CHECKED(2, "已校对"),
    EXECUTING(3, "执行中"),
    FINISHED(4, "已完成"),
    STOPPED(5, "已停止"),
    VOIDED(6, "已作废"),
    RETURNED(7, "已退回");

    private final int code;
    private final String label;

    CdrOrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrOrderStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrOrderStatusEnum e : values()) {
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
        CdrOrderStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        CdrOrderStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
