package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置属性（全局唯一，对应 his-pay.yml 的 alipay.*）。
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
     * 由患者端回调入口接收处理。必须是对公网可访问的 HTTPS 地址，
     * 且需与部署后实际路由前缀一致（当前控制器映射 /pay/notify/alipay）。
     * 对应 yml: alipay.notify-url。
     */
    private String notifyUrl;

    /**
     * 收费台出码（主扫）的回调地址，对应 yml: alipay.notify-url-cashier。
     *
     * <p>为什么单独一个：收到钱之后「往哪张账上记」由场景决定 —— 患者端的单要按业务类型找挂号单/押金，
     * 收费台的单直接记在账单上，两边的处理代码分属不同模块，只能靠两个不同的通知地址分流。
     * 两个地址都指向同一个应用，验签与金额闸门完全一致，只是入账出口不同。
     */
    private String notifyUrlCashier;

    /**
     * 是否走模拟支付。对应 yml: alipay.mock-enabled。
     *
     * <p>true=不出真码、不查真单：出码只给一张<b>扫不出任何东西的占位码</b>，查单只认「显式模拟过收款」的单。
     * 也就是说这一档<b>不会让任何一笔钱自己变成已收</b> —— 想让它到账必须另外调一次模拟收款接口
     * （{@code POST /charge/qrPay/mockPaid}，非模拟模式下那个接口直接拒绝）。
     * false=走真实支付宝 SDK。
     */
    private boolean mockEnabled = true;
}
