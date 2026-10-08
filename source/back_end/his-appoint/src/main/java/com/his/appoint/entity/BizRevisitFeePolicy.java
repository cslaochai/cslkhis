package com.his.appoint.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 复诊收费策略实体（复诊收费策略）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_revisit_fee_policy")
public class BizRevisitFeePolicy extends BaseEntity {

    /**
     * 策略名称（人看的）
     */
    private String policyName;

    /**
     * 复诊来源（1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊；0-不限）
     */
    private Integer revisitSource;

    /**
     * 与原就诊医生（0-不限 1-要求同一医生 2-要求不同医生）
     */
    private Integer sameDoctor;

    /**
     * 与原就诊科室（0-不限 1-要求同一科室 2-要求不同科室）
     */
    private Integer sameDept;

    /**
     * 与原就诊日最大间隔天数（NULL-不限）
     */
    private Integer withinDays;

    /**
     * 收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）
     */
    private Integer chargeMode;

    /**
     * 匹配优先级（数值小者优先）
     */
    private Integer priority;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
