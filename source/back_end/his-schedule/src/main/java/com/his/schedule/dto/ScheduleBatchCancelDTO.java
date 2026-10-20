package com.his.schedule.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 停诊批量退号入参
 */
@Data
public class ScheduleBatchCancelDTO {

    /**
     * 所属门诊排班ID（可空；空则只退号、不落排班变更日志）
     */
    private Long scheduleId;

    @NotEmpty(message = "挂号记录不能为空")
    private List<Long> registIds;

    /**
     * 原因
     */
    private String reason;
}
