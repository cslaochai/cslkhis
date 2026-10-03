package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 三级对账总结果。
 */
@Data
public class SettlementReconcileVO {

    /**
     * 对账日期（yyyy-MM-dd）
     */
    private String settleDate;

    /**
     * 各级结论
     */
    private List<ReconcileItemVO> items;

    /**
     * 总是否平（所有级次都平）
     */
    private Boolean passed;

    /**
     * 最大差额
     */
    private BigDecimal maxDiffAmount;

    /**
     * 未纳入任何班结单的已收费笔数（忘了交班的那些）
     */
    private Integer unassignedCount;

    /**
     * 未纳入班结的金额
     */
    private BigDecimal unassignedAmount;

    /**
     * 无科室归属的明细金额（科室对账的差异项）
     */
    private BigDecimal unattributedAmount;

    /**
     * 一句话结论
     */
    private String summary;
}
