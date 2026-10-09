package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性（全局唯一）
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx.pay")
public class WxPayProperties {

    private String appId;

    private String mchId;

    private String apiV3Key;

    private String certSerialNo;

    private String privateKeyPath;

    private String notifyUrl;

    private boolean mockEnabled = true;
}
