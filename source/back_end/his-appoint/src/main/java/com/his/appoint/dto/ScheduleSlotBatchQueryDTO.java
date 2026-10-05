package com.his.appoint.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量查询排班时间片段入参（日视图「医生 × 半小时段」看板用）。
 */
@Data
public class ScheduleSlotBatchQueryDTO {

    /**
     * 排班ID列表（排班信息的ID）；为空返回空列表
     */
    @Size(max = 500, message = "一次最多查询 500 条排班的时段")
    private List<Long> scheduleIds;
}
