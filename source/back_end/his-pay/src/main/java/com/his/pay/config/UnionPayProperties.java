package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 银联云闪付配置属性（全局唯一，对应 his-pay.yml 的 unionpay.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "unionpay")
public class UnionPayProperties {

    /**
     * 银联开放平台应用标识（appId），非敏感。对应 yml: unionpay.app-id。
     */
    private String appId;

    /**
     * 银联商户号（merId）。对应 yml: unionpay.mer-id。
     */
    private String merId;

    /**
     * 商户私钥（privateKey），用于请求签名，敏感。对应 yml: unionpay.private-key。
     */
    private String privateKey;

    /**
     * 银联平台公钥（publicKey），用于回调与响应验签。对应 yml: unionpay.public-key。
     */
    private String publicKey;

    /**
     * 银联网关地址，默认 https://gateway.95516.com。对应 yml: unionpay.gateway-url。
     */
    private String gatewayUrl = "https://gateway.95516.com";

    /**
     * 是否走模拟支付：true=后端直接判定支付成功（dev/演示），false=走真实银联 SDK。对应 yml: unionpay.mock-enabled。
     */
    private boolean mockEnabled = true;
}
