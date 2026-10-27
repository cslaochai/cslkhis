package com.his.charge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 医保结算与联网通道配置（his.insurance.*）。
 * <ul>
 *   <li>enabled：医保联网结算总开关。false = 不支持医保结算，结算时直接按自费原价走，不查医保；</li>
 *   <li>channel：联网通道。参保状态只认 remote（医保结算经办平台），mock 通道永远查不到参保。</li>
 *   <li>crypto：报文国密（SM2 签名 + SM4 加密 + csssl 证书链）。院内段密钥与前置机 mip.crypto.sm4-key-his 一致。</li>
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

    /**
     * 报文国密参数（his.insurance.crypto.*）：HIS→医保前置 方向 SM2 签名 + SM4 加密
     */
    private Crypto crypto = new Crypto();

    @Data
    public static class Channel {

        /**
         * 通道实现：remote=调医保结算经办平台（参保核验的唯一来源）；mock=报文/对账联调，查不到参保
         */
        private String mode = "remote";

        /**
         * 医保前置系统（csmip）根地址：HIS → csmip(8091) → csmis(8090, 医保局端mock)
         */
        private String baseUrl = "http://localhost:8091/mip";

        /**
         * 连接超时（毫秒）
         */
        private int connectTimeoutMs = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeoutMs = 15000;
    }

    @Data
    public static class Crypto {

        /**
         * 报文国密开关：true=SM2 签名 + SM4 加密 + csssl 证书链验签；false=明文（开发期）
         */
        private boolean enabled = false;

        /**
         * 院内段 SM4 会话密钥（32 位十六进制，与前置机 mip.crypto.sm4-key-his 一致；生产按会话协商）
         */
        private String sm4Key = "";

        /**
         * 本端 SM2 签名私钥 PEM（classpath 资源路径，csssl 签发）
         */
        private Resource privateKey;

        /**
         * 本端证书链 PEM（叶子 + SUB_CA，csssl 签发）
         */
        private Resource certChain;

        /**
         * 信任根：csssl 根证书 PEM
         */
        private Resource rootCa;

        /**
         * 信任链：csssl SUB_CA 证书 PEM
         */
        private Resource subCa;

        /**
         * 报文时间窗（秒），超窗按重放拒绝
         */
        private int clockSkewSeconds = 300;
    }
}
