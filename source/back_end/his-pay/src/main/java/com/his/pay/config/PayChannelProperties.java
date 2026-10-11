package com.his.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 收费台支付渠道总开关（全局唯一，对应 his-pay.yml 的 pay.channel.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.channel")
public class PayChannelProperties {

    /**
     * 收费台扫码支付总开关：true=启用第三方扫码支付，false=关闭。对应 yml: pay.channel.enabled，默认 true。
     */
    private boolean enabled = true;
}
