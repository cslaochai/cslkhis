package com.his.charge.service;

import java.io.Serializable;
import java.math.BigDecimal;

public interface PayRefundService {

    RefundReceipt refund(RefundRequest request);


    public record RefundRequest(Integer payMethod, String chargeNo, String refundNo,
                                BigDecimal amount, String reason) implements Serializable {
    }


    public record RefundReceipt(boolean success, String channelRefundNo, String errMsg) implements Serializable {

        public static RefundReceipt ok(String channelRefundNo) {
            return new RefundReceipt(true, channelRefundNo, null);
        }

        public static RefundReceipt fail(String errMsg) {
            return new RefundReceipt(false, null, errMsg);
        }
    }
}
