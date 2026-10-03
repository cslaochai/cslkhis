package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 护理质量指标月度台账（护理质控指标台账，sql/168）。
 *
 * <p><b>这是结果账，不是事实账</b>：每一行都由「检查表 / 不良事件 + 住院事实」算出来，
 * 随时可以用同一条口径重算覆盖（{@code /nursing/qc/recalc}）。
 * 所以台账里绝不放任何手填数字，分子分母都要能回答「从哪来」——
 * 备注写的就是来源（哪张检查单、床日多少）。
 *
 * <p>{@code uk_indicator(ward_id, stat_month, indicator_code)} 不含 del_flag ⇒ <b>删除走物理删</b>；
 * 重算用 {@code INSERT ... ON DUPLICATE KEY UPDATE}，一条表达式改两处（本表与铺底 SQL）会静默漂移，
 * 自检 T14~T16 专门盯这条。
 *
 * <p>report_status=2-已上报的行<b>重算跳过</b>：报出去的数字进了护理部月度通报，
 * 一键把它改掉等于事后无痕修订历史数据，要改先退回未上报。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_qc_indicator")
public class BizNursingQcIndicator extends BaseEntity {

    /** 病区ID */
    private Long wardId;
    /** 病区名称（快照） */
    private String wardName;
    /** 病区所属科室：数据范围按它收口 */
    private Long deptId;
    /** 科室名称（快照） */
    private String deptName;
    /** 统计月份 yyyy-MM */
    private String statMonth;
    /** NursingIndicatorEnum#getCode */
    private String indicatorCode;
    /** 指标名称（快照） */
    private String indicatorName;
    /** {@code %} 或例/千床日 */
    private String unit;
    /** 分子 */
    private BigDecimal numerator;
    /** 抽查例数（合格率类）或实际占用床日数（千床日类） */
    private BigDecimal denominator;
    /** 指标值 */
    private BigDecimal rateValue;
    /** 目标值，千床日类为 NULL（不硬拍常数） */
    private BigDecimal targetValue;
    /** 1-达标 0-未达标 NULL-无目标 */
    private Integer reachedFlag;
    /** 事实来源（1-检查表 2-不良事件+住院事实） */
    private Integer sourceType;
    /** NursingQcReportEnum：1-未上报 2-已上报 */
    private Integer reportStatus;
    /** 最近一次重算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calcTime;
}
