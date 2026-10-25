package com.his.outpatient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 按周期复制排班入参（把「上一周」整周搬到「下一周」）。
 */
@Data
public class StaffScheduleCopyDTO {

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
     * 来源区间开始日（含）
     */
    @NotNull(message = "来源区间开始日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromStartDate;

    /**
     * 来源区间结束日（含）
     */
    @NotNull(message = "来源区间结束日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromEndDate;

    /**
     * 目标区间开始日（含）
     */
    @NotNull(message = "目标区间开始日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toStartDate;

    /**
     * 排班域（1-岗位出勤 2-急诊值班）：复制只许在本域的单元内进行。
     * 为空=不校验（兼容内部调用）。
     */
    private Integer scope;
}
