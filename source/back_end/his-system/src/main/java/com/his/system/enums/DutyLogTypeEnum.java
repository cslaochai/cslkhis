package com.his.system.enums;

import lombok.Getter;

/**
 * 值班日志类型枚举（码值口径 = biz_duty_log.log_type 列注释）。
 */
@Getter
public enum DutyLogTypeEnum {

    EVENT(1, "值班事件"),
    LEFTOVER(2, "遗留事项"),
    PATROL(3, "巡查");

    private final int code;
    private final String label;

    DutyLogTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyLogTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyLogTypeEnum e : values()) {
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

    public static String getText(Integer code) {
        DutyLogTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。 */
    public static String labelOrUnknown(Integer code) {
        DutyLogTypeEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
