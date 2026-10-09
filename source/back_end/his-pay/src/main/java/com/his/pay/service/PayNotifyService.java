package com.his.pay.service;

/**
 * 支付回调处理服务（验签 + 更新支付状态 + 推进业务）
 */
public interface PayNotifyService {

    /**
     * 微信支付回调处理
     */
    void handleWxNotify(String body);

    /**
     * 支付宝支付回调处理
     */
    void handleAlipayNotify(String body);
}
