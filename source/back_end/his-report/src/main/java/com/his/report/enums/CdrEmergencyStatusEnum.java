package com.his.report.enums;

import lombok.Getter;

/**
 * CDR 时间轴：急诊状态文案（码值口径 = 库列注释）。
 */
@Getter
public enum CdrEmergencyStatusEnum {

    WAITING(1, "候诊"),
    IN_TREATMENT(2, "诊治中"),
    OBSERVATION(3, "留观"),
    TO_INPATIENT(4, "转住院"),
    LEAVE(5, "离院"),
    DEATH(6, "死亡");

    private final int code;
    private final String label;

    CdrEmergencyStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static CdrEmergencyStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CdrEmergencyStatusEnum e : values()) {
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
        CdrEmergencyStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或脏码值返回「未知(n)」，保留原始码值 */
    public static String labelOrUnknown(Integer code) {
        CdrEmergencyStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
