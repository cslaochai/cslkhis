package com.his.pharmacy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 生成抗菌药物监测指标入参。
 */
@Data
public class AntibioticStatsGenerateDTO {

    /** 统计月份 */
    @NotBlank(message = "统计月份不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "统计月份格式应为 yyyy-MM")
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    @NotNull(message = "统计范围不能为空")
    @Min(value = 1, message = "统计范围非法")
    @Max(value = 2, message = "统计范围非法")
    private Integer scopeType = 1;

    /** 备注 */
    private String remark;
}
