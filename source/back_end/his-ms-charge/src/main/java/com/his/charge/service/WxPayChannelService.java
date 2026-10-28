package com.his.charge.service;

import com.his.charge.vo.PayOrderVO;
import com.his.charge.service.PayChannelResult.PayUnifiedResult;
import com.his.charge.service.PayChannelResult.TradeState;

import java.math.BigDecimal;

public interface WxPayChannelService {

    PayUnifiedResult unifiedOrder(PayOrderVO order);

    /**
     * 主动查单：渠道侧的交易状态才是收款事实。
     *
     * <p>真模式尚未接通（Native 下单与 V3 验签解密都缺），调用会明确抛业务异常而不是返回 null ——
     * 把「查不了」说成「患者还没付」会让收银台一直停在等付款，而钱其实已经进了商户号。
     *
     * @return 渠道明确回答「没有这笔交易/尚未付款」时返回 null；
     * 调用本身失败（配置、签名、网络）一律抛业务异常
     */
    TradeState queryTrade(String payNo);

    /**
     * 渠道交易状态是否「钱已实收」。状态原文是微信 V3 协议的一部分，收口在渠道层，
     * 不让字面量散进业务模块（业务侧只问「算不算收到」）。
     */
    boolean isPaidStatus(String tradeStatus);

    /**
     * 模拟收款：替患者在模拟渠道把这单付掉。只有模拟模式允许，真模式直接拒绝 ——
     * 这个动作的产出是「渠道说钱到了」，拿它推进真实收款等于凭空造账。
     *
     * @param amount 模拟实收金额，按「患者照单全付」传入，查单时原样回吐给入账闸门比对
     */
    void markMockPaid(String payNo, BigDecimal amount);

}
