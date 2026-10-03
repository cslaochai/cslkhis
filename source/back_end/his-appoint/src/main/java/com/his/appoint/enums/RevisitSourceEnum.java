package com.his.appoint.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 复诊来源枚举 —— 回答「这是哪一种复诊」，决定占不占号源、按哪条策略收费。
 *
 * <p>与 visit_type（1 初诊 / 2 复诊）是从属关系：只有复诊才有来源；
 * 与 regist_type（号别 1 普通 / 2 专家 / 3 急诊 / 4 免费）仍是两个正交维度。
 */
@Getter
@AllArgsConstructor
public enum RevisitSourceEnum {

    /**
     * 1-当日回诊：同一次挂号的延续（拿检查结果回来复看）。
     * 不占号源、必须免收 —— 门诊「一次就诊 = 一次挂号」，重复收诊查费在医保飞检里算重复收费。
     */
    SAME_DAY_RETURN(1, "当日回诊"),

    /**
     * 2-医嘱复诊预约：医生开了复查医嘱，约未来的号，占号源。
     */
    DOCTOR_ORDERED(2, "医嘱复诊预约"),

    /**
     * 3-患者自助复诊：患者在小程序上从既往病历发起，占号源。
     */
    PATIENT_SELF(3, "患者自助复诊"),

    /**
     * 4-随访计划复诊：随访任务生成的复诊号（化疗、慢病、术后等计划内复诊）。
     */
    FOLLOWUP_PLAN(4, "随访计划复诊"),

    /**
     * 未知类型（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知类型");

    private final int code;

    private final String label;

    public static RevisitSourceEnum fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (RevisitSourceEnum item : values()) {
            if (item.code == code) {
                return item;
            }
        }
        return UNKNOWN;
    }

    /**
     * 是否为「不占号源」的复诊：只有当日回诊。
     * 其余三种都是新的一次就诊，必须选排班、扣号源，否则医生排班数与门诊日志会对不上。
     */
    public static boolean needsNoSchedule(Integer code) {
        return code != null && code == SAME_DAY_RETURN.code;
    }
}
