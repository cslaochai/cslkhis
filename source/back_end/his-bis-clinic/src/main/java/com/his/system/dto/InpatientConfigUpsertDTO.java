package com.his.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 住院 / 床位 / 转诊 / 住院证参数配置 upsert DTO（sys_config：inpatient.* / duty.coord.* / bed.* / admission_order.* 键）
 */
@Data
public class InpatientConfigUpsertDTO {

    @NotNull(message = "住院请假时长上限不能为空")
    @Min(value = 1, message = "住院请假时长上限必须大于 0")
    private Integer leaveMaxHours;

    @NotNull(message = "待床等待预警时长不能为空")
    @Min(value = 0, message = "待床等待预警时长不能为负数")
    private Integer bedWaitHours;

    @NotNull(message = "转院/转诊待处理预警时长不能为空")
    @Min(value = 0, message = "转院/转诊待处理预警时长不能为负数")
    private Integer referralPendingHours;

    @NotNull(message = "床位最长可等待天数不能为空")
    @Min(value = 1, message = "床位最长可等待天数必须大于 0")
    private Integer bedWaitMaxDays;

    @NotNull(message = "住院证有效期不能为空")
    @Min(value = 1, message = "住院证有效期必须大于 0")
    private Integer admissionOrderValidDays;
}
