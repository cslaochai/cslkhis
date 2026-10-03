package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保结算结果出参
 */
@Data
public class SettlementResultVO {

    /**
     * 医保结算清单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settlementId;

    /**
     * 总金额，单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 医保支付金额，单位：元
     */
    private BigDecimal insurancePay;

    /**
     * 个人账户支付金额，单位：元
     */
    private BigDecimal personalPay;

    /**
     * 自付金额，单位：元
     */
    private BigDecimal selfPay;

}
