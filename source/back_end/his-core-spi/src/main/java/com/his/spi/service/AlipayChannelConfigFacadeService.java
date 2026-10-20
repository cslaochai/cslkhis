package com.his.spi.service;

import com.his.spi.vo.AlipayChannelConfigVO;

/**
 * 支付宝渠道配置端口：需要直接向开放平台发起请求的调用方（如渠道账单拉取）取应用身份与网关。
 */
public interface AlipayChannelConfigFacadeService {

    /**
     * 取支付宝渠道配置。用于日终对账页拉取渠道账单前组装开放平台客户端。
     *
     * <p>返回内容含应用私钥：调用方只许拿去签名，禁止回传前端、禁止写进日志。
     */
    AlipayChannelConfigVO getChannelConfig();
}
