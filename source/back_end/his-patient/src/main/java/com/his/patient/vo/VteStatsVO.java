package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VTE 防控月度指标行（快照，分子分母一并带出供复核）
 */
@Data
public class VteStatsVO {

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

    private String scopeTypeText;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（快照）
     */
    private String deptName;

    /**
     * 同期出院患者数
     */
    private Integer dischargeCount;

    /**
     * 其中做过 Caprini 评估的患者数
     */
    private Integer assessedCount;

    /**
     * VTE 风险评估率（%）
     */
    private BigDecimal assessRate;

    /**
     * 其中最新评估为中高危
     */
    private Integer highRiskCount;

    /**
     * 中高危占比（%）
     */
    private BigDecimal highRiskRate;

    /**
     * 中高危中至少落实一条措施的患者数
     */
    private Integer preventDoneCount;

    /**
     * 预防措施落实率（%）
     */
    private BigDecimal preventRate;

    /**
     * 院内新发 VTE 患者数
     */
    private Integer vteEventCount;

    /**
     * 院内 VTE 发生率（%）
     */
    private BigDecimal vteIncidenceRate;

    /**
     * 预防相关出血患者数
     */
    private Integer bleedCount;

    /**
     * 目标值对照（等级评审/VTE 防治中心建设常用阈值；只作提示，不是判定）
     */
    private BigDecimal assessRateTarget;

    private BigDecimal preventRateTarget;

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
