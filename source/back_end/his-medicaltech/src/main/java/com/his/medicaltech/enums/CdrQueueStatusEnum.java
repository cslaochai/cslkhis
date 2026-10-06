package com.his.medicaltech.enums;

import lombok.Getter;

/**
 * CDR 时间轴：候诊队列状态文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrQueueStatusEnum {

    WAITING(2, "候诊中"),
    IN_CONSULT(3, "就诊中"),
    SEEN(4, "已就诊"),
    CANCELLED(5, "已退号"),
    MISSED(6, "已过号");

    private final int code;
    private final String label;

    CdrQueueStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrQueueStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrQueueStatusEnum e : values()) {
            if (e.code == code) {
                return e;
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
     * 展示用：null / 脏码值一律返回空串，绝不返回 null、绝不回落合法值
     */
    public static String getText(Integer code) {
        CdrQueueStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /**
     * 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值
     */
    public static String labelOrUnknown(Integer code) {
        CdrQueueStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
