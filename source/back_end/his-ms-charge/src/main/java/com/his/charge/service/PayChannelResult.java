package com.his.charge.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付通道层共用的结果载体。
 *
 * <p>微信 / 支付宝 / 云闪付三个通道的「下单结果」与「查单快照」结构完全一致，
 * 不属于任何单一通道，故抽为中立的公共类型，避免某个通道的实现去依赖另一个通道的内部 record。
 */
public final class PayChannelResult {

    private PayChannelResult() {
    }

    /**
     * 支付单下单结果。
     *
     * <p>没有「模拟支付成功」这一态：渠道模拟模式只可能给出一张<b>扫不实的占位码</b>（出码这一步放行），
     * 到账与否仍由查单说话，而模拟查单只认显式登记过的模拟收款。
     * 所以这里只有「拿到收银台参数」和「失败」两种结果，谁都不可能自己判成已收。
     */
    public record PayUnifiedResult(Map<String, String> payParams, String errMsg) {
        public static PayUnifiedResult cashier(Map<String, String> payParams) {
            return new PayUnifiedResult(payParams, null);
        }

        public static PayUnifiedResult fail(String errMsg) {
            return new PayUnifiedResult(null, errMsg);
        }
    }

    /**
     * 渠道交易状态快照。
     *
     * @param tradeNo     渠道交易号（原路退款要靠它）
     * @param tradeStatus 渠道交易状态原文
     * @param totalAmount 渠道侧订单金额，用于与本地支付单逐字比对
     */
    public record TradeState(String tradeNo, String tradeStatus, BigDecimal totalAmount) {
    }
}
