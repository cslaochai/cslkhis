package com.his.charge.vo;

import lombok.Data;

/**
 * 财务日结各状态计数（一次查全，供页面卡片用）。
 */
@Data
public class SettlementStatusCountVO {

    /**
     * 交班单：待日结
     */
    private Long cashierPending;

    /**
     * 交班单：已日结
     */
    private Long cashierSettled;

    /**
     * 交班单：已审核
     */
    private Long cashierAudited;

    /**
     * 日结单：待审核
     */
    private Long dayPendingAudit;

    /**
     * 日结单：已审核
     */
    private Long dayAudited;

    /**
     * 日结单：有差异
     */
    private Long dayWithDiff;

    /**
     * 未日结的交班单金额合计（财务最关心的一个数：还有多少钱没进日结）
     */
    private java.math.BigDecimal cashierPendingAmount;
}
