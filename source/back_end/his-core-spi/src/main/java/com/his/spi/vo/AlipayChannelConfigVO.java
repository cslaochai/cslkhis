package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付宝渠道配置快照：向开放平台发起一笔请求所需的应用身份、网关与验签公钥。
 *
 * <p>含应用私钥，只在本机服务间传递：禁止挂进任何接口出参、禁止整体写进日志。
 */
@Data
@NoArgsConstructor
public class AlipayChannelConfigVO {

    /**
     * 开放平台应用ID
     */
    private String appId;

    /**
     * 网关地址
     */
    private String gatewayUrl;

    /**
     * 支付宝公钥（应答与通知验签用）
     */
    private String alipayPublicKey;

    /**
     * 应用私钥（请求签名用，敏感，不得外泄）
     */
    private String privateKey;

    /**
     * 支付宝直连渠道开关（true-已启用 false-未启用）
     */
    private boolean enabled;
}
