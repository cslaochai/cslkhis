package com.his.appoint.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量查询排班时间片段入参（日视图「医生 × 半小时段」看板用）。
 *
 * <p>为什么要批量接口而不是循环调 {@code GET /schedule/slotList}：
 * 一天几十条排班逐条请求会打出一串请求，且整屏要等最慢那一格才出得来。
 * 号源的事实都在段上（半小时一档），看板格子要的是段级余号，一次查全即可。
 */
@Data
public class ScheduleSlotBatchQueryDTO {

    /**
     * 排班ID列表（排班信息的ID）；为空返回空列表
     */
    @Size(max = 500, message = "一次最多查询 500 条排班的时段")
    private List<Long> scheduleIds;
}
