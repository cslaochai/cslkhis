package com.his.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 按周期复制排班入参（把「上一周」整周搬到「下一周」）。
 *
 * <p>只按<b>星期</b>对齐：源区间长度必须与目标区间长度相同（7 天=整周搬运），
 * 否则「周一的班」落到目标区间里会随机错位，复制一次就要人工核对一整周。
 */
@Data
public class StaffScheduleCopyDTO {

    /**
     * 排班单元类型（1-科室 2-病区 3-全院）
     */
    @NotNull(message = "排班单元类型不能为空")
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
}
