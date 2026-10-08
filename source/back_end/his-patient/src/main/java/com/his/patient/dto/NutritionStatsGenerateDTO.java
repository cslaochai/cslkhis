package com.his.patient.dto;

import com.his.common.validation.InEnum;
import com.his.patient.enums.StatsScopeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成/重算营养膳食月度指标快照。
 */
@Data
public class NutritionStatsGenerateDTO {

    /** 统计月份 yyyy-MM */
    @NotBlank(message = "统计月份不能为空")
    private String statMonth;

    /** 1-全院一条 2-按有出院/有方案的科室各生成一条 */
    @NotNull(message = "统计范围不能为空")
    @InEnum(value = StatsScopeEnum.class, message = "统计范围取值不合法（1-全院 2-科室）")
    private Integer scopeType;
}
