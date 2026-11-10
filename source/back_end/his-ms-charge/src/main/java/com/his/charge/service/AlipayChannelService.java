package com.his.charge.service;

import com.his.charge.vo.PayOrderVO;
import com.his.charge.service.PayChannelResult.PayUnifiedResult;
import com.his.charge.service.PayChannelResult.TradeState;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付出口
 */
public interface AlipayChannelService {

    PayUnifiedResult unifiedOrder(PayOrderVO order);

    /**
     * 当面付预下单（主扫出码）：在支付宝侧落一笔待支付交易，返回可直接画成二维码的码串。
     *
     * <p>与 {@link #unifiedOrder} 的分工：那个返回 App 端调起参数，钱在客户端付；这个返回码串，
     * 钱由扫码人付，支付结果只来自异步通知或主动查单。同一商户订单号重复调用由渠道侧幂等返回同一张码，
     * 所以出码失败重试是安全的。
     *
     * <p>失败一律抛业务异常（渠道未启用、签名、网络、风控拦截），不返回空码：
     * 收银员手上要么有一张能付的码，要么明确知道为什么没有。
     */
    String precreate(PayOrderVO order);

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


}
