package com.his.outpatient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门诊排班「驳回」入参：把待审核的排班退回草稿，并附带驳回原因。
 */
@Data
public class ScheduleRejectDTO {

    /**
     * 排班行ID
     */
    @NotNull(message = "排班行ID不能为空")
    private Long scheduleId;

    /**
     * 驳回原因（留痕展示，便于排班人知道为什么被打回）
     */
    private String reason;
}
