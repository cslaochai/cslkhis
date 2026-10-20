package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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

    /**
     * 码值是否合法（写入侧校验用；null 不合法）
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }

    /**
     * 该层级是否必须配响应形态。一线/二线/三线必须配（否则排了班不知道人是驻守还是在家），
     * 不适用档位不配。
     */
    public static boolean requiresAttendMode(Integer code) {
        return code != null && code != NONE.code;
    }

    public static DutyLevelEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (DutyLevelEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        DutyLevelEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        DutyLevelEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        DutyLevelEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
