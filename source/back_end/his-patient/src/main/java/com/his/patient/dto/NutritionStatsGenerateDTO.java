package com.his.patient.dto;

import com.his.common.validation.InEnum;
import com.his.patient.enums.StatsScopeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 生成/重算营养膳食月度指标快照。
 *
 * <p>同月同范围覆盖同一行（uk_nutrition_stats），不留历史版本 ——
 * 指标是"这个月的事实"，重算只应该让数字更准，不应该多出一套互相打架的快照。
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
