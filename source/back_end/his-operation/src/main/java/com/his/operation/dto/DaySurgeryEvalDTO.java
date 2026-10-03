package com.his.operation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 日间手术术前评估入参。
 *
 * <p>评估不通过（evalResult=2）时状态仍留在「待评估」可重评 —— 直接推到终态会让
 * 「换个麻醉方式再评估一次」这种真实场景无处落单；但**不通过一律不得安排手术**。
 */
@Data
public class DaySurgeryEvalDTO implements Serializable {

    @NotNull(message = "登记单ID不能为空")
    private Long id;

    /** 术前评估结论（1-通过 2-不通过） */
    @NotNull(message = "评估结论不能为空")
    private Integer evalResult;

    /** 评估意见/禁忌筛查结果 */
    @NotBlank(message = "评估意见不能为空（禁忌筛查结论要留痕）")
    private String evalRemark;
}
