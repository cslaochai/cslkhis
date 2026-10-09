package com.his.pay.service;

import java.math.BigDecimal;

/**
 * 收费台支付渠道出口（微信扫码/支付宝扫码）
 */
public interface ChargePayChannelService {

    /**
     * 付款码支付结果
     */
    record PayResult(boolean success, String channelTxnNo, String errMsg) {}

    /**
     * 付款码支付（扫患者手机上的付款码）
     *
     * @param authCode 付款码（微信/支付宝扫出来的字符串）
     * @param orderNo 商户订单号（biz_settlement_bill.bill_no）
     * @param amount 金额（元）
     * @param subject 商品描述
     * @param channel 渠道（1-微信 2-支付宝）
     * @return 支付结果
     */
    PayResult microPay(String authCode, String orderNo, BigDecimal amount, String subject, Integer channel);

    /**
     * 原路退款
     *
     * @param channelTxnNo 渠道交易号（支付成功时返回的）
     * @param refundNo 退款单号
     * @param amount 退款金额
     * @param reason 退款原因
     * @param channel 渠道（1-微信 2-支付宝）
     * @return 是否成功
     */
    boolean refund(String channelTxnNo, String refundNo, BigDecimal amount, String reason, Integer channel);
}
