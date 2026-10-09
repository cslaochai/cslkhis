package com.his.common.config.pay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 收费台支付渠道总开关（全局唯一）
 */
@Data
@Component
@ConfigurationProperties(prefix = "pay.channel")
public class PayChannelProperties {

    private boolean enabled = true;
}
