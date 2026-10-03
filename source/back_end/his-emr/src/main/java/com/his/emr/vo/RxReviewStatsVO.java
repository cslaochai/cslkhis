package com.his.emr.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 处方点评月度统计 VO（点评率/不合理处方率/超常处方数 —— 评审口径核心指标） */
@Data
public class RxReviewStatsVO {

    /** 统计月份（yyyy-MM） */
    private String month;

    /** 同期已审核/已发药处方总数（分母） */
    private Long totalPrescriptions;

    /** 已点评处方数 */
    private Long reviewedCount;

    /** 处方点评率（%，reviewedCount / totalPrescriptions） */
    private BigDecimal reviewRate;

    /** 不合理处方数（结论 2/3/4） */
    private Long unreasonableCount;

    /** 不合理处方率（%，unreasonableCount / reviewedCount） */
    private BigDecimal unreasonableRate;

    /** 超常处方数（结论 4） */
    private Long abnormalCount;

    /** 已公示数 */
    private Long publicityCount;

    /** 待点评数（期内批次中未点评明细） */
    private Long pendingCount;
}
