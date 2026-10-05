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

    public static String labelOf(Integer code) {
        DutyLogTypeEnum e = fromCode(code);
        return e == null ? "未知(" + code + ")" : e.getLabel();
    }
}
