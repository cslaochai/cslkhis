package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 营养膳食月度指标行。
 */
@Data
public class NutritionStatsVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 统计月份
     */
    private String statMonth;

    /**
     * 统计范围（1-全院 2-科室）
     */
    private Integer scopeType;

    /** 统计范围文案 */
    private String scopeTypeText;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 同期出院患者数（筛查率/会诊率分母）
     */
    private Integer dischargeCount;

    /**
     * 其中出院前做过 NRS2002 筛查的患者数
     */
    private Integer screenedCount;

    /**
     * 营养风险筛查率（%）
     */
    private BigDecimal screenRate;

    /**
     * 筛查阳性
     */
    private Integer riskCount;

    /**
     * 筛查阳性率（%）
     */
    private BigDecimal riskRate;

    /**
     * 膳食方案总数
     */
    private Integer dietPlanCount;

    /**
     * 其中营养科已接收
     */
    private Integer dietConfirmCount;

    /**
     * 膳食医嘱执行率（%）
     */
    private BigDecimal dietConfirmRate;

    /**
     * 营养会诊单数
     */
    private Integer consultCount;

    /** 按时限完成营养会诊的单数 */
    private Integer consultOnTimeCount;

    /** 营养会诊按时率（%） */
    private BigDecimal consultOnTimeRate;

    /**
     * 订餐明细数
     */
    private Integer mealOrderCount;

    /**
     * 其中已签收的明细数
     */
    private Integer mealSignedCount;

    /**
     * 订餐签收率（%）
     */
    private BigDecimal mealSignRate;

    /**
     * 退订明细数
     */
    private Integer mealCancelCount;

    /**
     * 目标值对照（等级评审常用阈值；只作提示，不是判定）
     */
    private BigDecimal screenRateTarget;

    /** 膳食方案确认率目标（%） */
    private BigDecimal dietConfirmRateTarget;

    /** 营养会诊按时率目标（%） */
    private BigDecimal consultOnTimeRateTarget;

    /** 订餐签收率目标（%） */
    private BigDecimal mealSignRateTarget;

    /**
     * 生成人
     */
    private String generateBy;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 备注
     */
    private String remark;
}
