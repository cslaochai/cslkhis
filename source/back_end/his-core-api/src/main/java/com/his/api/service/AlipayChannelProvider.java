package com.his.api.service;

import com.his.api.vo.PayOrderVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付出口
 */
public interface AlipayChannelProvider {

    WxPayChannelProvider.PayUnifiedResult unifiedOrder(PayOrderVO order);

    /**
     * 主扫下单（面对面二维码）：生成一张只能用支付宝扫的收款码，扫开即是支付宝收银台。
     *
     * <p>与 {@link #unifiedOrder} 的区别是产品码不同、由谁发起扫码不同：本方法是<b>医院出码、患者扫</b>，
     * 码内容（{@code payParams} 里的 {@code qrCode}）是支付宝的收款链接，不是网页地址，
     * 因此<b>微信扫不开它</b> —— 一码只能属于一个渠道，界面必须先选渠道再生成码。
     *
     * <p>同一张支付单重复调用是安全的：支付宝按商户订单号幂等返回同一张码，不会重复占款。
     *
     * @return {@code payParams.qrCode}=待渲染成图片的码串；失败时 {@code errMsg} 非空
     */
    WxPayChannelProvider.PayUnifiedResult precreate(PayOrderVO order);

    /**
     * 取回这张单已出过的码串，没有（没出过、或已过期被清）返回 null。
     *
     * <p>存在的理由就是别把「同一张码」问渠道两遍：码串在有效期内对同一商户订单号恒定不变，
     * 出过一次就该按单号留在缓存里，收银员点刷新、前端重连、页面重开都只该本地重画。
     * 缓存活着而支付单还待支付，说明渠道侧那笔预下单也还活着（两侧有效期同口径）。
     */
    String recallQrCode(String payNo);

    /**
     * 关闭渠道侧交易（患者放弃付款、收银员换渠道出码时必须调）。
     *
     * <p>不关就等于把「一个订单号可以付一次」的额度一直悬在渠道侧：患者事后拿旧码扫出来照样能付，
     * 而本地这张单早就作废了，钱进来无人认领。渠道关单失败只留痕不抛异常 ——
     * 本地单已经关闭是既成事实，不能因为渠道一句失败就把界面上的关闭动作退回去。
     */
    boolean closeTrade(String payNo);

    /**
     * 异步通知验签。只回答「这条通知是不是支付宝签的、是不是发给本应用的」，
     * 不判断业务是否该入账 —— 金额、订单归属、状态推进一律由调用方结合本地账单再判。
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 主动查单。渠道侧的交易状态才是收款事实，异步通知只是一个敲门信号。
     *
     * @return 渠道明确回答「没有这笔交易/尚未付款」时返回 null；
     * 调用本身失败（配置、签名、网络）一律抛业务异常 —— 把「没查成」说成「还没付」，
     * 界面就会一路停在「等待患者支付」，而钱其实已经在渠道里了
     */
    TradeState queryTrade(String payNo);

    /**
     * 渠道交易状态是否「钱已实收」。状态原文是支付宝协议的一部分，收口在渠道层，
     * 不让字面量散进业务模块（业务侧只问「算不算收到」）。
     */
    boolean isPaidStatus(String tradeStatus);

    /**
     * 模拟收款：替患者在模拟渠道把这单付掉。
     *
     * <p>只有渠道处于模拟模式时允许调用，真模式调用直接拒绝 —— 这个动作的产出是「渠道说钱到了」，
     * 拿它去推进真实收款等于凭空造账。模拟出来的交易号带 {@code SIMU-} 前缀，
     * 与商户平台真实流水号同位存放，因此勾对链路照跑，但谁都看得出它不是资金核对依据。
     *
     * @param amount 模拟实收金额，按「患者照单全付」传入，查单时原样回吐给入账闸门比对
     */
    void markMockPaid(String payNo, BigDecimal amount);

    /**
     * 渠道侧交易状态快照。
     *
     * @param tradeNo     支付宝交易号（原路退款要靠它）
     * @param tradeStatus 渠道交易状态原文
     * @param totalAmount 渠道侧订单金额，用于与本地支付单逐字比对
     */
    record TradeState(String tradeNo, String tradeStatus, BigDecimal totalAmount) {
    }
}
