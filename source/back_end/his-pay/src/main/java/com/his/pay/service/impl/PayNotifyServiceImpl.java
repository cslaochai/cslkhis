package com.his.pay.service.impl;

import com.his.pay.service.PayNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 支付回调处理实现（验签 + 更新支付状态 + 推进业务）
 */
@Service
public class PayNotifyServiceImpl implements PayNotifyService {

    private static final Logger log = LoggerFactory.getLogger(PayNotifyServiceImpl.class);

    @Override
    public void handleWxNotify(String body) {
        // TODO: 微信支付回调验签 + 解析 out_trade_no / transaction_id / trade_state
        // 1. 用 WxPayService 验签（需要商户证书）
        // 2. 解析出 outTradeNo、channelTxnNo、tradeState
        // 3. 根据 outTradeNo 找到本地支付单
        // 4. tradeState == "SUCCESS" → 调用 handlePaySuccess
        log.warn("[微信支付回调] 待实现，body={}", body);
    }

    @Override
    public void handleAlipayNotify(String body) {
        // TODO: 支付宝回调验签 + 解析 trade_no / out_trade_no / trade_status
        // 1. 用 AlipayProperties.alipayPublicKey 验签
        // 2. 解析 out_trade_no、trade_no、trade_status
        // 3. trade_status == "TRADE_SUCCESS" → 调用 handlePaySuccess
        log.warn("[支付宝支付回调] 待实现，body={}", body);
    }
}
