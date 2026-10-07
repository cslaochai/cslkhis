package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出院队列费用（{@code StatReportAggMapper#cohortFees}）。
 *
 * <p>口径：L2 出院结算账单（{@code bill_type = 4}），排除 4-已作废。
 * "个人自付"取账单上的 {@code payable_amount}（应由患者承担的部分），
 * "欠费"是它减去已收 —— 账单上可能同时存在"已收够"和"还欠"两种状态，报表要能分开看。
 */
@Data
public class StatCohortFeesRowVO implements Serializable {

    /**
     * 出院结算单笔数
     */
    private Long settleCount;

    /**
     * 费用总额
     */
    private BigDecimal totalAmount;

    /**
     * 医保支付（统筹）
     */
    private BigDecimal insuranceAmount;

    /**
     * 个人自付（应由患者承担）
     */
    private BigDecimal patientPayAmount;

    /**
     * 欠费（个人自付 - 已收，钳 0）
     */
    private BigDecimal arrearsAmount;
}