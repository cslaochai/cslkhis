package com.his.pay.service;

import com.his.pay.entity.BizPayOrder;

import java.util.Map;

public interface WxPayChannelService {

    PayUnifiedResult unifiedOrder(BizPayOrder order);

    /**
     * 支付单结果。
     */
    public record PayUnifiedResult(boolean mockPaid, Map<String, String> payParams, String errMsg) {
        public static PayUnifiedResult mock() {
            return new PayUnifiedResult(true, null, null);
        }

        public static PayUnifiedResult cashier(Map<String, String> payParams) {
            return new PayUnifiedResult(false, payParams, null);
        }

        public static PayUnifiedResult fail(String errMsg) {
            return new PayUnifiedResult(false, null, errMsg);
        }
    }
}
