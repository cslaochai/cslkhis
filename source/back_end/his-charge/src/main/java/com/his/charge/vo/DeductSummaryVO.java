package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 扣款台账汇总（后端按 SQL 聚合，禁止前端用当前页自造统计数字）。
 */
@Data
public class DeductSummaryVO {

    /**
     * 待确认单数
     */
    private Integer pendingConfirmCount;

    /**
     * 申诉中单数
     */
    private Integer appealingCount;

    /**
     * 维持扣款待缴单数
     */
    private Integer waitPayCount;

    /**
     * 已缴回（已结案）单数
     */
    private Integer paidCount;

    /**
     * 超期未结单数（待确认/申诉中且已过处理期限）
     */
    private Integer overdueCount;

    /**
     * 未结案扣款金额合计（状态 1/2/4）
     */
    private BigDecimal openAmountSum;

    /**
     * 已缴回金额合计
     */
    private BigDecimal paidAmountSum;
}
