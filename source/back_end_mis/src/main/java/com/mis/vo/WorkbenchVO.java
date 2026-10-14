package com.mis.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 经办工作台汇总
 */
@Data
public class WorkbenchVO implements Serializable {

    private Todo todo;
    private Kpi kpi;
    private List<BillVO> recentBills;

    @Data
    public static class Todo implements Serializable {
        private int pendingSettle;
        private int audit;
        private int reconcile;
        private int refund;
        /** 待核实监管疑点 */
        private int suspicion;
        /** 待追回单数 */
        private int recover;
        private int total;
    }

    @Data
    public static class Kpi implements Serializable {
        private int todayCount;
        private BigDecimal todayAmount;
        private BigDecimal insurancePay;
        private BigDecimal personalAccount;
        private BigDecimal cashPay;
    }
}
