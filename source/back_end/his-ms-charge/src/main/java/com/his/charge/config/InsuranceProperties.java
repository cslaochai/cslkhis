package com.his.charge.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 医保结算与联网通道配置（his.insurance.*）。
 * <ul>
 *   <li>enabled：医保联网结算总开关。false = 不支持医保结算，结算时直接按自费原价走，不查医保；</li>
 *   <li>channel：联网通道。参保状态只认 remote（医保结算经办平台），mock 通道永远查不到参保。</li>
 *   <li>crypto：报文国密（SM2 签名 + SM4 加密 + csssl 证书链）。院内段密钥与前置机 mig.crypto.sm4-key-his 一致。</li>
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

    /**
     * 证书改成外部文件后，「没挂上目录」是部署事故而不是运行期业务失败：启动就查文件在不在，别等第一笔医保结算才炸。
     */
    @PostConstruct
    public void assertCryptoResourcesReadable() {
        if (!crypto.isEnabled()) {
            return;
        }
        requireReadable("his.insurance.crypto.private-key", crypto.getPrivateKey());
        requireReadable("his.insurance.crypto.cert-chain", crypto.getCertChain());
        requireReadable("his.insurance.crypto.root-ca", crypto.getRootCa());
        requireReadable("his.insurance.crypto.sub-ca", crypto.getSubCa());
    }

    private void requireReadable(String key, Resource resource) {
        if (resource == null || !resource.isReadable()) {
            throw new IllegalStateException("医保国密证书读取失败，请检查配置 " + key + "=" + resource);
        }
    }

    @Data
    public static class Channel {

        /**
         * 通道实现：remote=调医保结算经办平台（参保核验的唯一来源）；mock=报文/对账联调，查不到参保
         */
        private String mode = "remote";

        /**
         * 医保前置系统（csmig）根地址：HIS → csmig(8091) → csmis(8090, 医保局端mock)
         */
        private String baseUrl = "http://localhost:8091/mig";

        /**
         * 连接超时（毫秒）
         */
        private int connectTimeoutMs = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeoutMs = 15000;

        /**
         * 前置系统接口路径（his.insurance.channel.paths.*），相对 base-url
         */
        private Paths paths = new Paths();
    }

    /**
     * 医保前置系统各能力的接口路径：与前置机 GatewayController 的路由契约一一对应
     */
    @Data
    public static class Paths {

        /**
         * 通用报文上送路径，{msgType} 由业务报文类型替换
         */
        private String send = "/gateway/{msgType}";

        /**
         * 日对账账单拉取路径
         */
        private String dayBill = "/gateway/dayBill";

        /**
         * 参保人信息查询路径
         */
        private String personQuery = "/insure/person/query";

        /**
         * 预结算（2201）路径
         */
        private String preSettle = "/gateway/2201";

        /**
         * 正式结算（2301）路径
         */
        private String formalSettle = "/gateway/2301";

        /**
         * 实时结算冲正（2305）路径
         */
        private String cancel = "/gateway/2305";
    }

    @Data
    public static class Crypto {

        /**
         * 报文国密开关：true=SM2 签名 + SM4 加密 + csssl 证书链验签；false=明文（开发期）
         */
        private boolean enabled = false;

        /**
         * 院内段 SM4 会话密钥（32 位十六进制，与前置机 mig.crypto.sm4-key-his 一致；生产按会话协商）
         */
        private String sm4Key = "";

        /**
         * 本端 SM2 签名私钥 PEM（外部文件路径，file: 前缀，csssl 签发；不进 jar）
         */
        private Resource privateKey;

        /**
         * 本端证书链 PEM（叶子 + SUB_CA，外部文件路径，csssl 签发）
         */
        private Resource certChain;

        /**
         * 信任根：csssl 根证书 PEM（外部文件路径）
         */
        private Resource rootCa;

        /**
         * 信任链：csssl SUB_CA 证书 PEM（外部文件路径）
         */
        private Resource subCa;

        /**
         * 报文时间窗（秒），超窗按重放拒绝
         */
        private int clockSkewSeconds = 300;
    }
}
