package com.his.appoint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模板段级号源配置项（跟随 {@link ScheduleTemplateUpsertDTO} 整批提交）。
 */
@Data
public class ScheduleTemplateSlotItemDTO {

    /**
     * 段开始（HH:mm，半小时整点）
     */
    @NotBlank(message = "段开始时间不能为空")
    private String startTime;

    /**
     * 段结束（HH:mm，半小时整点）
     */
    @NotBlank(message = "段结束时间不能为空")
    private String endTime;

    /**
     * 段号源总数
     */
    @NotNull(message = "段号源数不能为空")
    @Min(value = 0, message = "段号源数不能为负")
    private Integer totalSource;

    /**
     * 段内线上预约预留（0=未划池，不得超过段号源数）
     */
    @Min(value = 0, message = "段预约池不能为负")
    private Integer appointmentSource;
}
