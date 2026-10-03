package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 生成月度 VTE 防控指标快照 */
@Data
public class VteStatsGenerateDTO {

    /** 统计月份 */
    @NotBlank(message = "统计月份不能为空")
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    @NotNull(message = "统计范围不能为空")
    private Integer scopeType;
}
