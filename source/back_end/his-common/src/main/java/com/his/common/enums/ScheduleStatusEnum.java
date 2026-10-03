package com.his.common.enums;

import lombok.Getter;

/**
 * 出诊计划状态枚举（号源侧，与出勤状态不是一件事）
 *
 * <p><b>停诊不等于停班</b>：停诊说的是「这个班不放号」，医生可能人还在科里（临时停诊、号源约满改现场），
 * 而出勤状态说的是「这个人今天来不来」。所以停诊只锁号源池，不改动在岗名单 ——
 * 把停诊写成停班，值班名单会少一个本来能接到电话的人。
 */
@Getter
public enum ScheduleStatusEnum {

    /** 停诊：号源池与时间段全停，不可再挂 */
    STOPPED(0, "停诊"),
    /** 正常：可挂号 */
    NORMAL(1, "正常"),
    /** 已满：号源挂完 */
    FULL(2, "已满"),
    /** 已过期：排班日期已过 */
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

    public static String labelOf(Integer code) {
        ScheduleStatusEnum status = fromCode(code);
        return status == null ? "未知(" + code + ")" : status.getLabel();
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
