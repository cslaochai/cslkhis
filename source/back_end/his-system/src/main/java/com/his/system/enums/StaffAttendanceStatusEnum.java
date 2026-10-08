package com.his.system.enums;

import lombok.Getter;

/**
 * 出勤状态（1-正常 2-迟到 3-早退 4-缺勤 5-替班 6-加班 7-支援）。
 */
@Getter
public enum StaffAttendanceStatusEnum {

    NORMAL(1, "正常"),
    LATE(2, "迟到"),
    EARLY_LEAVE(3, "早退"),
    ABSENT(4, "缺勤"),
    SUBSTITUTE(5, "替班"),
    OVERTIME(6, "加班"),
    SUPPORT(7, "支援");

    private final int code;
    private final String label;

    StaffAttendanceStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static StaffAttendanceStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StaffAttendanceStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** 展示用：脏值与 null 一律空串，不冒充某个合法档位 */
    public static String getText(Integer code) {
        StaffAttendanceStatusEnum e = fromCode(code);
        return e == null ? "" : e.label;
    }

    /** 异常 / 审计用：null 或不在枚举内返回「未知(n)」，保留原始码值便于排查脏数据 */
    public static String labelOrUnknown(Integer code) {
        StaffAttendanceStatusEnum e = fromCode(code);
        return e == null ? (code == null ? "未知" : "未知(" + code + ")") : e.label;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }
}
