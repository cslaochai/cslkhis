package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 支付宝开放平台应用 AppID，非敏感。对应 yml: alipay.app-id。
     */
    private String appId;

    /**
     * 应用私钥（privateKey），用于请求签名，敏感。对应 yml: alipay.private-key。
     */
    private String privateKey;

    /**
     * 支付宝公钥（alipayPublicKey），用于回调验签与响应验签。对应 yml: alipay.alipay-public-key。
     */
    private String alipayPublicKey;

    /**
     * 支付宝网关地址，默认正式环境 https://openapi.alipay.com/gateway.do。对应 yml: alipay.gateway-url。
     */
    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

    /**
     * 支付结果异步回调地址（notifyUrl）。支付宝在用户支付完成后会把结果推送到该地址，
     */
    private String notifyUrl;

    /**
     * 收费台出码（主扫）的回调地址，对应 yml: alipay.notify-url-cashier。
     */
    private String notifyUrlCashier;

    /**
     * 是否走模拟支付。对应 yml: alipay.mock-enabled。
     */
    private boolean mockEnabled = true;
}
