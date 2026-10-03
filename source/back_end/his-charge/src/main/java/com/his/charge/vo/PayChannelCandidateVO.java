package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 勾对候选行：只给对账页要用的那几列，账单一号 + 流水号 + 金额就够认出这笔钱。
 */
@Data
public class PayChannelCandidateVO {

    private String txnNo;

    private Integer direction;

    private String directionText;

    private String billNo;

    private String patientName;

    private BigDecimal amount;

    private String txnTime;
}
