package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付方式分桶聚合（一个支付方式一行）。
 */
@Data
public class PaymentMethodSumVO implements Serializable {

    /**
     * 支付方式（{@code PaymentMethodEnum}）：
     * 1-现金 2-微信 3-支付宝 4-医保个账 5-院内余额 6-银行卡 7-转账；null 表示历史数据缺失。
     *
     * <p>6/7 与null 都由 Service 归入「未知渠道」，绝不并进微信/支付宝假装有数。
     */
    private Integer paymentMethod;

    /**
     * 该渠道的收款笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 该渠道的收款金额合计（元）
     */
    private BigDecimal amount;
}