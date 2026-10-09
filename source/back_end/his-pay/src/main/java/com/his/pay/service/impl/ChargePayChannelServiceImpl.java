package com.his.pay.service.impl;

import com.his.common.config.pay.AlipayProperties;
import com.his.common.config.pay.PayChannelProperties;
import com.his.common.config.pay.UnionPayProperties;
import com.his.pay.service.ChargePayChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 收费台支付渠道实现（微信/支付宝/云闪付扫码 - 付款码支付）
 */
@Service
public class ChargePayChannelServiceImpl implements ChargePayChannelService {
    private static final Logger log = LoggerFactory.getLogger(ChargePayChannelServiceImpl.class);

    private final AlipayProperties alipayProperties;
    private final UnionPayProperties unionPayProperties;
    private final PayChannelProperties payChannelProperties;

    public ChargePayChannelServiceImpl(AlipayProperties alipayProperties,
                                       UnionPayProperties unionPayProperties,
                                       PayChannelProperties payChannelProperties) {
        this.alipayProperties = alipayProperties;
        this.unionPayProperties = unionPayProperties;
        this.payChannelProperties = payChannelProperties;
    }

    @Override
    public PayResult microPay(String authCode, String orderNo, BigDecimal amount, String subject, Integer channel) {
        if (!payChannelProperties.isEnabled()) {
            throw new IllegalStateException(
                    "收费台支付渠道已关闭（pay.channel.enabled=false），无法调用支付。" +
                    "如需启用支付，请在 application-local.yml 中将 pay.channel.enabled 改为 true。"
            );
        }

        try {
            if (channel == 1) {
                // TODO: 微信付款码支付需要 WxJava 完整实现，当前先留桩
                log.warn("[微信支付] 付款码支付完整实现待补（需要商户号 + APIv3密钥）");
                return new PayResult(false, null, "微信支付暂未完整实现");
            } else if (channel == 2) {
                return alipayMicroPay(authCode, orderNo, amount, subject);
            } else if (channel == 3) {
                // TODO: 云闪付付款码支付待接 SDK
                log.warn("[云闪付支付] 付款码支付完整实现待补");
                return new PayResult(false, null, "云闪付支付暂未完整实现");
            } else {
                return new PayResult(false, null, "不支持的支付渠道：" + channel);
            }
        } catch (Exception e) {
            log.error("[收费台支付] 付款码支付失败 orderNo={} channel={} err={}", orderNo, channel, e.getMessage(), e);
            return new PayResult(false, null, e.getMessage());
        }
    }

    @Override
    public boolean refund(String channelTxnNo, String refundNo, BigDecimal amount, String reason, Integer channel) {
        if (!payChannelProperties.isEnabled()) {
            throw new IllegalStateException(
                    "收费台支付渠道已关闭（pay.channel.enabled=false），无法调用退款。" +
                    "如需启用退款，请在 application-local.yml 中将 pay.channel.enabled 改为 true。"
            );
        }

        try {
            if (channel == 1) {
                log.warn("[微信支付] 退款完整实现待补");
                return false;
            } else if (channel == 2) {
                return alipayRefund(channelTxnNo, refundNo, amount, reason);
            } else {
                log.warn("[收费台支付] 不支持的退款渠道：{}", channel);
                return false;
            }
        } catch (Exception e) {
            log.error("[收费台支付] 退款失败 channelTxnNo={} channel={} err={}", channelTxnNo, channel, e.getMessage(), e);
            return false;
        }
    }

    private PayResult alipayMicroPay(String authCode, String orderNo, BigDecimal amount, String subject) throws Exception {
        log.info("[支付宝支付] 付款码支付 orderNo={} amount={} authCode={}", orderNo, amount, maskAuthCode(authCode));

        com.alipay.api.AlipayClient client = new com.alipay.api.DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json", "UTF-8",
                alipayProperties.getAlipayPublicKey(),
                "RSA2"
        );

        com.alipay.api.request.AlipayTradePayRequest request = new com.alipay.api.request.AlipayTradePayRequest();
        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"scene\":\"payment_code\",\"auth_code\":\"%s\",\"total_amount\":\"%s\",\"subject\":\"%s\"}",
                orderNo, authCode, amount.toString(), subject
        );
        request.setBizContent(bizContent);

        com.alipay.api.response.AlipayTradePayResponse response = client.execute(request);

        if (response.isSuccess()) {
            log.info("[支付宝支付] 付款码支付成功 orderNo={} tradeNo={}", orderNo, response.getTradeNo());
            return new PayResult(true, response.getTradeNo(), null);
        } else {
            return new PayResult(false, null, "支付宝付款码支付失败: " + response.getSubMsg());
        }
    }

    private boolean alipayRefund(String channelTxnNo, String refundNo, BigDecimal amount, String reason) throws Exception {
        log.info("[支付宝支付] 退款 tradeNo={} refundNo={} amount={}", channelTxnNo, refundNo, amount);

        com.alipay.api.AlipayClient client = new com.alipay.api.DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json", "UTF-8",
                alipayProperties.getAlipayPublicKey(),
                "RSA2"
        );

        com.alipay.api.request.AlipayTradeRefundRequest request = new com.alipay.api.request.AlipayTradeRefundRequest();
        String bizContent = String.format(
                "{\"trade_no\":\"%s\",\"out_request_no\":\"%s\",\"refund_amount\":\"%s\",\"refund_reason\":\"%s\"}",
                channelTxnNo, refundNo, amount.toString(), reason
        );
        request.setBizContent(bizContent);

        var response = client.execute(request);
        return response.isSuccess();
    }

    private String maskAuthCode(String authCode) {
        if (authCode == null || authCode.length() <= 6) {
            return "***";
        }
        return authCode.substring(0, 3) + "****" + authCode.substring(authCode.length() - 3);
    }
}
