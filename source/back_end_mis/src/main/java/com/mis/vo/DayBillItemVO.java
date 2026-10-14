package com.mis.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 日对账“对方账”明细（HIS 端 RemoteSettlement 同构）
 */
@Data
public class DayBillItemVO {

    private String tradeNo;
    private String settlementNo;
    private BigDecimal totalAmount;
    private BigDecimal insurancePay;
    private String receiptNo;
}
