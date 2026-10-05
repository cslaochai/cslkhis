package com.his.appoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 段级号源编辑入参（/schedule/slotUpsert）：一次提交一个排班的若干段。
 */
@Data
public class ScheduleSlotUpsertDTO {

    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    /**
     * 要编辑的段列表（按段ID定位）
     */
    @NotEmpty(message = "时间段列表不能为空")
    @Valid
    private List<ScheduleSlotItemUpsertDTO> slots;
}
