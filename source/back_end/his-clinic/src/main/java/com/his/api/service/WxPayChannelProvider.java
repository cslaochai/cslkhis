package com.his.api.service;

import com.his.api.vo.PayOrderVO;

import java.util.Map;

public interface WxPayChannelProvider {

    PayUnifiedResult unifiedOrder(PayOrderVO order);

    /**
     * 主扫出码（微信 Native）：码串放在 {@code payParams} 的 {@code qrCode} 键上，
     * 与支付宝出码同一个键名，渲染侧不必按渠道分支（各渠道的码串本来就是不同的链接格式）。
     */
    PayUnifiedResult nativePrecreate(PayOrderVO order);

    /**
     * 支付单下单结果。
     *
     * <p>没有「模拟支付成功」这一态：渠道模拟模式只可能给出一张<b>扫不实的占位码</b>（出码这一步放行），
     * 到账与否仍由查单说话，而模拟查单只认显式登记过的模拟收款。
     * 所以这里只有「拿到收银台参数」和「失败」两种结果，谁都不可能自己判成已收。
     */
    record PayUnifiedResult(Map<String, String> payParams, String errMsg) {
        public static PayUnifiedResult cashier(Map<String, String> payParams) {
            return new PayUnifiedResult(payParams, null);
        }

        public static PayUnifiedResult fail(String errMsg) {
            return new PayUnifiedResult(null, errMsg);
        }
    }
}
