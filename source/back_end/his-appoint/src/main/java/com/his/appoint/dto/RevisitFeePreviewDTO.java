package com.his.appoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 复诊费用预估入参
 *
 * <p>提交挂号<b>之前</b>问一次「这张复诊号要交多少钱」。判定口径必须与
 * {@code addAppoint} 用同一个 decide，否则会出现「预览 12 元、结账 0 元」这种
 * 患者已在小程序上确认过金额的错位。
 */
@Data
public class RevisitFeePreviewDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者主键不能为空")
    private Long patientId;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）
     */
    @NotNull(message = "复诊来源不能为空")
    @Min(value = 1, message = "复诊来源不合法")
    @Max(value = 4, message = "复诊来源不合法")
    private Integer revisitSource;

    /**
     * 原病历ID（收费策略的间隔天数、同医生/同科室判定都以它为基准）
     */
    @NotNull(message = "原病历不能为空")
    private Long revisitRecordId;

    /**
     * 拟预约的排班ID；只有「当日回诊」允许为空（它不占号源、恒为 0 元）
     */
    private Long scheduleId;
}
