package com.his.common.config.pay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 云闪付支付配置属性（全局唯一）
 */
@Data
@Component
@ConfigurationProperties(prefix = "unionpay")
public class UnionPayProperties {

    private String appId;

    private String merId;

    private String privateKey;

    private String publicKey;

    private String gatewayUrl = "https://gateway.95516.com";

    private boolean mockEnabled = true;
}
