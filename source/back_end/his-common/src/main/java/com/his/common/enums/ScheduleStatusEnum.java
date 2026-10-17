package com.his.common.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 出诊计划状态枚举（号源侧，与出勤状态不是一件事）
 */
@Getter
public enum ScheduleStatusEnum {

    /**
     * 停诊：号源池与时间段全停，不可再挂
     */
    STOPPED(0, "停诊"),
    /**
     * 正常：可挂号
     */
    NORMAL(1, "正常"),
    /**
     * 已满：号源挂完
     */
    FULL(2, "已满"),
    /**
     * 已过期：排班日期已过
     */
    EXPIRED(3, "已过期");

    private final int code;
    private final String label;

    ScheduleStatusEnum(int code, String label) {
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
     * 是否处于「可挂号」状态。null 按不可挂处理（脏数据不放号）。
     */
    public static boolean bookable(Integer code) {
        return code != null && code == NORMAL.code;
    }

    /**
     * 是否停诊（号源池停用）。
     */
    public static boolean stopped(Integer code) {
        return code != null && code == STOPPED.code;
    }

    public static ScheduleStatusEnum fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (ScheduleStatusEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

    // 展示口径：脏码值出空串；要看原始码值走 getLabelOrUnknown
    public static String getLabel(Integer code) {
        ScheduleStatusEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }

    public static String getLabel(Integer code, String defaultValue) {
        ScheduleStatusEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }

    public static String getLabelOrUnknown(Integer code) {
        ScheduleStatusEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
}
