package com.his.appoint.dto;

import lombok.Data;

/**
 * 排班状态更新入参（停诊/启用，只更新状态列）
 */
@Data
public class ScheduleStatusUpsertDTO {

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 状态（0-停诊 1-启用）
     */
    private Integer status;
}
