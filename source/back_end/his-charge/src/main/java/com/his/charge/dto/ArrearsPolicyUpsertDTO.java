package com.his.charge.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 欠费管控策略保存入参（单行）。
 *
 * <p>预警线/停费线允许为空（空=未设置），但一旦填写不允许为负。
 */
@Data
public class ArrearsPolicyUpsertDTO {

    /**
     * 预警线（元）：欠费达线提示，不拦截
     */
    @DecimalMin(value = "0", message = "预警线不能为负数")
    private BigDecimal warnLine;

    /**
     * 停费线（元）：欠费达线拦截择期类新开医嘱
     */
    @DecimalMin(value = "0", message = "停费线不能为负数")
    private BigDecimal stopLine;

    /**
     * 停费管控开关（0-关 1-开）
     */
    @NotNull(message = "停费管控开关不能为空")
    @Min(value = 0, message = "停费管控开关只能为 0 或 1")
    @Max(value = 1, message = "停费管控开关只能为 0 或 1")
    private Integer stopEnabled;

    /**
     * 被拦截的医嘱类别（2-检查 3-检验 4-治疗）
     */
    private String stopClasses;

    /**
     * 备注
     */
    private String remark;
}
