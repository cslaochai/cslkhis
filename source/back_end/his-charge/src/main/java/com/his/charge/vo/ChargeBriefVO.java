package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算预览里的一张费用单（逐单列出，便于核对）。
 */
@Data
public class ChargeBriefVO implements Serializable {

    /**
     * 收费单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chargeId;

    /**
     * 收费单号
     */
    private String chargeNo;

    /**
     * 该单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 该单统筹支付
     */
    private BigDecimal insurancePayAmount;

    /**
     * 该单患者应付
     */
    private BigDecimal patientPayAmount;

    /**
     * 该单结算方式
     */
    private Integer settlementMode;

    /**
     * 该单结算方式文案
     */
    private String settlementModeName;
}
