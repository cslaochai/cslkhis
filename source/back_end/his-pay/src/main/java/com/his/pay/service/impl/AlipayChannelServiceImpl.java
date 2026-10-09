package com.his.pay.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.his.common.config.pay.AlipayProperties;
import com.his.pay.entity.BizPayOrder;
import com.his.pay.enums.PayOrderBizTypeEnum;
import com.his.pay.service.AlipayChannelService;
import com.his.pay.service.WxPayChannelService;
import com.his.pay.service.WxPayChannelService.PayUnifiedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝支付出口（支付宝 SDK 真实现）
 */
@Service
public class AlipayChannelServiceImpl implements AlipayChannelService {
    private static final Logger log = LoggerFactory.getLogger(AlipayChannelServiceImpl.class);

    private final AlipayProperties alipayProperties;

    public AlipayChannelServiceImpl(AlipayProperties alipayProperties) {
        this.alipayProperties = alipayProperties;
    }

    @Override
    public WxPayChannelService.PayUnifiedResult unifiedOrder(BizPayOrder order) {
        if (alipayProperties.isMockEnabled()) {
            throw new IllegalStateException(
                    "支付宝支付处于模拟模式（alipay.mock-enabled=true），无法调用真实支付。" +
                    "如需测试真实支付，请在 application-local.yml 中配置支付宝凭据并将 mock-enabled 改为 false。"
            );
        }

        try {
            log.info("[支付宝支付] 调支付宝统一下单（alipay.trade.app.pay） payNo={} amount={}",
                    order.getPayNo(), order.getAmount());

            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayProperties.getGatewayUrl(),
                    alipayProperties.getAppId(),
                    alipayProperties.getPrivateKey(),
                    "json", "UTF-8",
                    alipayProperties.getAlipayPublicKey(),
                    "RSA2"
            );

            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            com.alibaba.fastjson.JSONObject bizContent = new com.alibaba.fastjson.JSONObject();
            bizContent.put("out_trade_no", order.getPayNo());
            bizContent.put("total_amount", order.getAmount().toString());
            bizContent.put("subject", PayOrderBizTypeEnum.getText(order.getBizType()));
            bizContent.put("product_code", "QUICK_MSECURITY_PAY");
            request.setBizContent(bizContent.toString());

            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);

            if (!response.isSuccess()) {
                return WxPayChannelService.PayUnifiedResult.fail(
                        "支付宝下单失败: " + response.getSubMsg()
                );
            }

            Map<String, String> payParams = Map.of("orderInfo", response.getBody());
            log.info("[支付宝支付] 下单成功 payNo={}", order.getPayNo());
            return WxPayChannelService.PayUnifiedResult.cashier(payParams);

        } catch (AlipayApiException e) {
            log.error("[支付宝支付] 下单失败 payNo={} err={}", order.getPayNo(), e.getMessage(), e);
            return WxPayChannelService.PayUnifiedResult.fail(e.getMessage());
        }
    }
}
