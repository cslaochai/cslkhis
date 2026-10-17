package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /** Integer 码值判定：null 安全，语义同 == 比较 int 常量 */
    public boolean is(Integer code) {
        return code != null && code == this.code;
    }

    public static StaffAttendanceStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (StaffAttendanceStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        StaffAttendanceStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        StaffAttendanceStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        StaffAttendanceStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
