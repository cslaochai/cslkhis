package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 医保结算与联网通道配置（his.insurance.*）。
 * <ul>
 *   <li>enabled：医保联网结算总开关。false = 不支持医保结算，结算时直接按自费原价走，不查医保；</li>
 *   <li>channel：联网通道。参保状态只认 remote（医保结算经办平台），mock 通道永远查不到参保。</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.insurance")
public class InsuranceProperties {

    /**
     * 医保联网结算总开关：true=支持医保结算（按 channel.mode 远程查参保）；
     * false=不支持医保结算，结算时按自费原价走，不查医保
     */
    private boolean enabled = true;

    /**
     * 联网通道参数（his.insurance.channel.*）
     */
    private Channel channel = new Channel();

    @Data
    public static class Channel {

        /**
         * 通道实现：remote=调医保结算经办平台（参保核验的唯一来源）；mock=报文/对账联调，查不到参保
         */
        private String mode = "remote";

        /**
         * 医保结算经办平台根地址（remote 模式）
         */
        private String baseUrl = "http://localhost:8090/mss";

        /**
         * 连接超时（毫秒）
         */
        private int connectTimeoutMs = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeoutMs = 15000;
    }
}
