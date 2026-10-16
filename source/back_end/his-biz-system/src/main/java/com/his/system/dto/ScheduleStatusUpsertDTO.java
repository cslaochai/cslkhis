package com.his.system.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;
}
