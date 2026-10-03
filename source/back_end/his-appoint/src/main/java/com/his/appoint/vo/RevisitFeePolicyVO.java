package com.his.appoint.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 复诊收费策略列表/详情出参
 */
@Data
public class RevisitFeePolicyVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 策略名称
     */
    private String policyName;

    /**
     * 复诊来源（0-不限 1-当日回诊 2-医嘱复诊预约 3-患者自助复诊 4-随访计划复诊）
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
     * 与原就诊日最大间隔天数（null=不限）
     */
    private Integer withinDays;

    /**
     * 收费方式（1-全额收费 2-免挂号费 3-免挂号费+诊查费）
     */
    private Integer chargeMode;

    /**
     * 匹配优先级
     */
    private Integer priority;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
