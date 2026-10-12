package com.his.system.vo;

import lombok.Data;

/**
 * 住院 / 床位 / 转诊 / 住院证参数配置 VO（来源 sys_config：inpatient.* / duty.coord.* / bed.* / admission_order.* 键）
 */
@Data
public class InpatientConfigVO {

    private Integer leaveMaxHours;

    private Integer bedWaitHours;

    private Integer referralPendingHours;

    private Integer bedWaitMaxDays;

    private Integer admissionOrderValidDays;
}
