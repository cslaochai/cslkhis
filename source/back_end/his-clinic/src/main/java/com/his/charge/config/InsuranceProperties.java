package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 医保结算与联网通道配置（his.insurance.*）。
 * <ul>
 *   <li>enabled：医保联网结算总开关。false = 不支持医保结算，挂号/结算直接拒医保改自费，不查任何参保库；</li>
 *   <li>channel：联网通道（mock 查 HIS 本地参保台账镜像 / remote 调医保结算经办平台）。</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "his.insurance")
public class InsuranceProperties {

    /**
     * 医保联网结算总开关：true=支持医保结算（按 channel.mode 查 MIS 或本地镜像台账）；
     * false=不支持医保结算，挂号/结算直接拒医保改自费，不查任何参保库
     */
    private boolean enabled = true;

    /**
     * 联网通道参数（his.insurance.channel.*）
     */
    private Channel channel = new Channel();

    @Data
    public static class Channel {

        /**
         * 通道实现：remote=调医保结算经办平台（MIS）；mock=查 HIS 本地参保台账镜像
         */
        private String mode = "mock";

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
