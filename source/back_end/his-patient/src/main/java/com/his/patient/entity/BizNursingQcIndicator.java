package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 护理质量指标月度台账（护理质控指标台账，sql/168）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_qc_indicator")
public class BizNursingQcIndicator extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 病区ID
     */
    private Long wardId;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 病区所属科室：数据范围按它收口
     */
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 统计月份 yyyy-MM
     */
    private String statMonth;
    /**
     * NursingIndicatorEnum#getCode
     */
    private String indicatorCode;
    /**
     * 指标名称
     */
    private String indicatorName;
    /**
     * {@code %} 或例/千床日
     */
    private String unit;
    /**
     * 分子
     */
    private BigDecimal numerator;
    /**
     * 抽查例数（合格率类）或实际占用床日数（千床日类）
     */
    private BigDecimal denominator;
    /**
     * 指标值
     */
    private BigDecimal rateValue;
    /**
     * 目标值，千床日类为 NULL（不硬拍常数）
     */
    private BigDecimal targetValue;
    /**
     * 1-达标 0-未达标 NULL-无目标
     */
    private Integer reachedFlag;
    /**
     * 事实来源（1-检查表 2-不良事件+住院事实）
     */
    private Integer sourceType;
    /**
     * NursingQcReportEnum：1-未上报 2-已上报
     */
    private Integer reportStatus;
    /**
     * 最近一次重算时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calcTime;
}
