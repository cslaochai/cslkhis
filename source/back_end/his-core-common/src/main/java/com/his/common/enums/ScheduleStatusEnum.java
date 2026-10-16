package com.his.common.enums;

import lombok.Getter;

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

    public static ScheduleStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScheduleStatusEnum status : values()) {
            if (status.code == code) {
                return status;
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
        ScheduleStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
    }

    /**
     * 异常 / 审计用：null 或不在枚举内返回「未知(n)」（null 本身渲染成「未知」），保留原始码值便于排查脏数据。
     */
    public static String labelOrUnknown(Integer code) {
        ScheduleStatusEnum item = code == null ? null : fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
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

    public static String whitelistText() {
        StringBuilder sb = new StringBuilder();
        for (ScheduleStatusEnum status : values()) {
            sb.append(sb.length() == 0 ? "" : " / ").append(status.code).append("-").append(status.label);
        }
        return sb.toString();
    }
}
