package com.his.appoint.dto;

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
    private Integer consultStatus;
}
