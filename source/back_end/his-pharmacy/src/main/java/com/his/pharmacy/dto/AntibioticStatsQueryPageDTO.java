package com.his.pharmacy.dto;

import com.his.common.base.PageParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 已生成的抗菌药物监测指标分页入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AntibioticStatsQueryPageDTO extends PageParam {

    /** 统计月份 yyyy-MM */
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "统计月份格式应为 yyyy-MM")
    private String statMonth;

    /** 统计范围（1-全院 2-科室） */
    @Min(value = 1, message = "统计范围非法")
    @Max(value = 2, message = "统计范围非法")
    private Integer scopeType;
}
