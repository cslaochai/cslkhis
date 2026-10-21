package com.his.charge.config;

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
     * 收费台主扫的专用回调地址，对应 yml: alipay.notify-url-cashier。
     *
     * <p><b>当前无消费方</b>：收费台出的是聚合支付码，不再走支付宝原生 Native 下单，
     * 这个专供主扫的 notifyUrl 没有地方可传（患者端走 {@link #notifyUrl}）。
     * 保留配置项是给真实聚合网关落地后按需启用 —— 别误以为改了它就会生效。
     */
    private String notifyUrlCashier;

    /**
     * 是否启用支付宝直连支付：true=调支付宝 SDK 发起交易；false=渠道关闭，发起入口直接拒绝。
     * 已发生交易的查单、关单与回调验签不受此开关影响 —— 关掉开关不能让已经收进来的钱无处入账。
     * 对应 yml: alipay.enabled。
     */
    private boolean enabled = false;
}
