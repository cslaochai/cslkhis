package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术离院登记入参（术后观察 → 已出院）。
 *
 * <p>离院方式只收 1按时离院 / 3非计划再入院；2转普通住院走独立动作
 * （必须回填住院号 admission_id，那是医保与病案口径的分界点）。
 */
@Data
public class DaySurgeryDischargeDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /** 离院方式（1-按时离院 2-转普通住院 3-非计划再入院） */
    @NotNull(message = "离院方式不能为空")
    private Integer leaveType;

    /** 离院时间 yyyy-MM-dd HH:mm:ss（不传取当前时间） */
    private String dischargeTime;

    /** 出院评估结论 / 医嘱交代 */
    private String dischargeRemark;
}
