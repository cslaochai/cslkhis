package com.his.common.enums;

import lombok.Getter;

/**
 * 值班层级枚举（sql/202，字典 his_duty_level）
 */
@Getter
public enum DutyLevelEnum {

    /**
     * 不适用：行政总值班等无层级概念的点位
     */
    NONE(0, "不适用"),
    /**
     * 一线：住院医师，驻守病区，现场处置
     */
    FIRST(1, "一线"),
    /**
     * 二线：主治及以上，听班，一线叫了才到
     */
    SECOND(2, "二线"),
    /**
     * 三线：主任/副主任，听班兜底
     */
    THIRD(3, "三线");

    private final int code;
    private final String label;

    DutyLevelEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static DutyLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DutyLevelEnum level : values()) {
            if (level.code == code) {
                return level;
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

    public static String getText(Integer code) {
        DutyLevelEnum level = fromCode(code);
        return level == null ? "未知(" + code + ")" : level.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        DutyLevelEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }

    /**
     * 该层级是否必须配响应形态。一线/二线/三线必须配（否则排了班不知道人是驻守还是在家），
     * 不适用档位不配。
     */
    public static boolean requiresAttendMode(Integer code) {
        return code != null && code != NONE.code;
    }

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (DutyLevelEnum level : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(level.code).append("-").append(level.label);
        }
        return sb.toString();
    }
}
