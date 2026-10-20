package com.his.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 全院岗位排班「整周提交 / 整周审核发布」入参：定位到某个排班单元 + 某一周。
 */
@Data
public class StaffScheduleWeekActionDTO {

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    @NotNull(message = "排班单元类型不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "排班单元类型取值不合法")
    private Integer orgType;

    /**
     * 排班单元ID（全院级不传）
     */
    private Long orgId;

    /**
     * 目标周任意一天（落在哪一周就处理哪一周，按周一～周日收口）；为空按当前周
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;
}
