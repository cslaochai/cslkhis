package com.his.patient.vo;

import lombok.Data;

/**
 * 住院 / 床位 / 转诊 / 住院证参数配置 VO（来源 sys_config：inpatient.* / duty.coord.* / bed.* / admission_order.* 键）
 */
@Data
public class InpatientConfigVO {

    /** 住院请假时长上限（小时）：超出按请假超期处理 */
    private Integer leaveMaxHours;

    /** 待床等待预警时长（小时）：患者等床超过该时长提示协调 */
    private Integer bedWaitHours;

    /** 转院/转诊待处理预警时长（小时）：转出后未落地超过该时长提示跟进 */
    private Integer referralPendingHours;

    /** 床位最长可等待天数（天）：超过按等床超期处理 */
    private Integer bedWaitMaxDays;

    /** 住院证有效期（天）：过期未办理入院需医生重新开证 */
    private Integer admissionOrderValidDays;
}
