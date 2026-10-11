package com.his.system.api.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账单简要信息。
 */
@Data
@NoArgsConstructor
public class BillBriefVO {
    private Long billId;
    private String billNo;
    /**
     * 账单状态，字典 {@code his_bill_status}：1-待支付 2-部分支付 3-已支付 4-已作废 5-已退费
     */
    private Integer billStatus;
    /**
     * 个人应缴（统筹与个账已扣掉）
     */
    private BigDecimal payableAmount;
    /**
     * 净已收（收款合计 - 退款合计）
     */
    private BigDecimal paidAmount;
}