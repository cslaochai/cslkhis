package com.his.common.enums;

import lombok.Getter;

/**
 * 值班层级枚举（sql/202，字典 {@code his_duty_level}）
 *
 * <p><b>它回答的是「一件事升级到谁手上」，跟班内主副（{@link DutyRoleTypeEnum}）是两回事</b>：
 * 主班/副班是「同一个位上谁顶着」，层级是「一线处理不了就升二线，二线处理不了升三线」的责任档位。
 * 一个点位的 role_type 恒为主班，但 duty_level 可以是一线也可以是三线 —— 两者语义正交，不能合并。
 *
 * <p><b>0-不适用必须存在</b>：行政总值班、急诊总值班这些老点位没有层级概念，
 * 建表时给了默认值 0。字典里缺 0，老点位在页面上就会显示成「未知(0)」。
 *
 * <p><b>层级决定响应形态</b>，但不互相替代：一线是留院值班（人在医院），
 * 二线三线是听班（在家待命，叫了才到）。见 {@link AttendModeEnum}。
 */
@Getter
public enum DutyLevelEnum {

    /** 不适用：行政总值班等无层级概念的点位 */
    NONE(0, "不适用"),
    /** 一线：住院医师，驻守病区，现场处置 */
    FIRST(1, "一线"),
    /** 二线：主治及以上，听班，一线叫了才到 */
    SECOND(2, "二线"),
    /** 三线：主任/副主任，听班兜底 */
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

    public static String labelOf(Integer code) {
        DutyLevelEnum level = fromCode(code);
        return level == null ? "未知(" + code + ")" : level.getLabel();
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
