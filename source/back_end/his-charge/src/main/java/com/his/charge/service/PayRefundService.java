package com.his.charge.service;

import java.io.Serializable;
import java.math.BigDecimal;

public interface PayRefundService {

    RefundReceipt refund(RefundRequest request);

    /**
     * @param payMethod 原支付方式（字典 {@code his_pay_method}），决定分发给哪个渠道
     * @param chargeNo  原收款单号，真渠道要靠它反查原交易流水
     * @param refundNo  院内退费单号，作为渠道侧幂等键（同一单号重复请求不得重复退款）
     * @param amount    退款金额，必须大于 0
     * @param reason    退款原因（渠道侧留档，真接口一般限长，由本类截断）
     */
    public record RefundRequest(Integer payMethod, String chargeNo, String refundNo,
                                BigDecimal amount, String reason) implements Serializable {
    }

    /**
     * @param success         渠道是否受理
     * @param channelRefundNo 渠道退费流水号（成功时非空，写进台账供对账）
     * @param errMsg          失败原因（先截断再落库，别把超长异常拼进窄列）
     */
    public record RefundReceipt(boolean success, String channelRefundNo, String errMsg) implements Serializable {

        public static RefundReceipt ok(String channelRefundNo) {
            return new RefundReceipt(true, channelRefundNo, null);
        }

        public static RefundReceipt fail(String errMsg) {
            return new RefundReceipt(false, null, errMsg);
        }
    }
}
