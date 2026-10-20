package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 班别枚举（班次字典上的「属于哪个时段」，门诊号源侧按它归类与展示）
 */
@Getter
public enum ScheduleTypeEnum {

    MORNING(1, "上午"),
    AFTERNOON(2, "下午"),
    ALL_DAY(3, "全天"),
    EARLY_MORNING(4, "凌晨"),
    /**
     * 16:00~23:00 这类不跨零点的夜间班（前夜班/大夜班靠班次名区分，班别只到"夜班"一档）
     */
    NIGHT(5, "夜班");

    private final int code;
    private final String label;

    ScheduleTypeEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ScheduleTypeEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ScheduleTypeEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ScheduleTypeEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ScheduleTypeEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ScheduleTypeEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
