package com.his.spi.service;

import com.his.spi.vo.AggPaidInfoVO;

import java.math.BigDecimal;

/**
 * 收款台扫码支撑端口：聚合支付码的出码内容拼装、模拟收款登记与查单，以及已出码串的缓存。
 *
 * <p>聚合码不绑渠道，一张支付单对应一个码串，所以这里全部按支付单号取用。
 */
public interface QrPaySupportFacadeService {

    /**
     * 把支付单号拼成患者可扫的聚合收款链接。用于收费台扫码收款弹层出码。
     */
    String buildQrContent(String payNo);

    /**
     * 聚合收款是否处于模拟模式。真实模式下拒绝一切模拟收款动作。
     */
    boolean isMockEnabled();

    /**
     * 把一张码登记为「渠道已实收」。只用于联调与验收时替患者付款，真实模式直接报错。
     *
     * @param channel 这笔模拟收款落在哪个资金渠道（影响流水与对账口径），null 由实现方按默认渠道登记
     */
    void markMockPaid(String payNo, Integer channel, BigDecimal amount);

    /**
     * 查聚合码的模拟收款登记结果。用于收款结果轮询兜底入账。
     *
     * @return 没登记过、或真实模式（收款事实只来自网关通知）返回 null
     */
    AggPaidInfoVO queryMockPaid(String payNo);

    /**
     * 取回这张单已出过的码串；没出过、已过期或缓存不可用都返回 null（调用方退回真出码）。
     */
    String getQrCode(String payNo);

    /**
     * 记住这张单的码串。空白码串不写。
     */
    void putQrCode(String payNo, String qrCode);

    /**
     * 丢弃码串。用于关闭待支付码时同步清掉，避免界面重画出一张渠道侧早已关掉的码。
     */
    void evictQrCode(String payNo);
}
