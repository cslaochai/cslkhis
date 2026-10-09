package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置属性（全局唯一）
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private String appId;

    private String privateKey;

    private String alipayPublicKey;

    private String gatewayUrl = "https://openapi.alipay.com/gateway.do";

    private boolean mockEnabled = true;
}
