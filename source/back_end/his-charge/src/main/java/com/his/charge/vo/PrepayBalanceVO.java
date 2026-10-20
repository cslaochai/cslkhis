package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 预交金余额出参。
 */
@Data
public class PrepayBalanceVO implements Serializable {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 充值合计
     */
    private BigDecimal rechargeTotal;

    /**
     * 柜面退款合计（正数；出院退差转入院内余额的不算在这里）
     */
    private BigDecimal refundTotal;

    /**
     * 住院资金账户余额（充值 − 退款 − 结算抵扣 − 出院退差，由账户流水 SUM 现算）
     */
    private BigDecimal balance;

    /**
     * 流水条数
     */
    private Integer flowCount;
}
