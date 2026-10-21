package com.his.outpatient.dto;

import com.his.common.validation.AllowedValues;
import lombok.Data;

/**
 * 更新排班接诊状态入参
 */
@Data
public class ScheduleConsultStatusUpsertDTO {

    /**
     * 排班ID
     */
    private Long scheduleId;

    /**
     * 接诊状态（0-待开始 1-接诊中 2-暂停）
     */
    @AllowedValues(values = {"0", "1", "2"}, message = "接诊状态取值不合法")
    private Integer consultStatus;
}
