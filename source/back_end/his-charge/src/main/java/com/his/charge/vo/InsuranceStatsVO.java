package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 医保结算统计出参
 */
@Data
public class InsuranceStatsVO {

    /**
     * 今日结算总金额，单位：元
     */
    private BigDecimal todayTotal;

    /**
     * 今日医保支付金额，单位：元
     */
    private BigDecimal todayInsurancePay;

    /**
     * 今日结算单数量
     */
    private Integer todayCount;

    /**
     * 待结算数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pendingCount;

    /**
     * 已结算数量
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settledCount;

}
