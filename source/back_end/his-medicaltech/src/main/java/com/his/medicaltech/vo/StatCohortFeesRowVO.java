package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出院队列费用（StatReportAggMapper#cohortFees）。
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