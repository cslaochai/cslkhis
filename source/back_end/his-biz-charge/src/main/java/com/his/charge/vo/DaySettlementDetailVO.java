package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 日结单详情（日结单 + 当日交班单 + 三级对账 + 科室收入）。
 */
@Data
public class DaySettlementDetailVO {

    /**
     * 日结单本体（尚未日结时可能为 null，此时下面几块是"试算"结果）
     */
    private DaySettlementVO settlement;

    /**
     * 当日交班单
     */
    private List<CashierSettlementVO> shifts;

    /**
     * 三级对账结果
     */
    private SettlementReconcileVO reconcile;

    /**
     * 科室收入（末行为「无科室归属」，unattributed=true）
     */
    private List<DeptIncomeVO> deptIncomes;

    /**
     * 全部明细金额（= 已归属 + 无归属）
     */
    private BigDecimal totalDetailAmount;
}
