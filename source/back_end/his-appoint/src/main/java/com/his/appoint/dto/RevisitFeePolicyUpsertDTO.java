package com.his.appoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 复诊收费策略新增/修改入参
 */
@Data
public class RevisitFeePolicyUpsertDTO {

    /**
     * 策略ID，新增时为空
     */
    private Long id;

    /**
     * 策略名称
     */
    @NotBlank(message = "策略名称不能为空")
    private String policyName;

    /**
     * 复诊来源（0-不限 1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）
     */
    @NotNull(message = "复诊来源不能为空")
    @Min(value = 0, message = "复诊来源不合法")
    @Max(value = 4, message = "复诊来源不合法")
    private Integer revisitSource;

    /**
     * 与原就诊医生（0-不限 1-要求同一医生 2-要求不同医生），空默认不限
     */
    @Min(value = 0, message = "同一医生条件不合法")
    @Max(value = 2, message = "同一医生条件不合法")
    private Integer sameDoctor;

    /**
     * 与原就诊科室（0-不限 1-要求同一科室 2-要求不同科室），空默认不限
     */
    @Min(value = 0, message = "同一科室条件不合法")
    @Max(value = 2, message = "同一科室条件不合法")
    private Integer sameDept;

    /**
     * 与原就诊日最大间隔天数（空=不限）。
     * <p>填了它，「原病历不存在/没就诊日期」的复诊就匹配不到本条 —— 判不准一律按不命中处理，
     * 宁可多收费，不能凭空免收。
     */
    @Min(value = 0, message = "间隔天数不能为负")
    private Integer withinDays;

    /**
     * 收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）
     */
    @NotNull(message = "收费方式不能为空")
    @Min(value = 1, message = "收费方式不合法")
    @Max(value = 3, message = "收费方式不合法")
    private Integer chargeMode;

    /**
     * 匹配优先级（数值小者优先），空默认 100
     */
    @Min(value = 1, message = "优先级最小为 1")
    private Integer priority;

    /**
     * 状态（0-停用 1-启用），空默认启用
     */
    @Min(value = 0, message = "状态不合法")
    @Max(value = 1, message = "状态不合法")
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
