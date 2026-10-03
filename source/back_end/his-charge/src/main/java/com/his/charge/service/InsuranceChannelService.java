package com.his.charge.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InsuranceChannelService {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundMessage implements Serializable {
        /**
         * 医保接口编号：2304-结算信息上传 2305-结算信息撤销
         */
        private String msgType;
        /**
         * HIS 侧流水号（唯一）
         */
        private String tradeNo;
        /**
         * 报文全文 JSON
         */
        private String payload;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Receipt implements Serializable {
        private boolean success;
        /**
         * 医保端回执编号
         */
        private String receiptNo;
        /**
         * 回执报文全文 JSON
         */
        private String replyPayload;
        /**
         * 失败原因（success=false 时有值）
         */
        private String errMsg;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoteSettlement implements Serializable {
        private String tradeNo;
        private String settlementNo;
        private BigDecimal totalAmount;
        private BigDecimal insurancePay;
        private String receiptNo;
    }

    Receipt send(OutboundMessage message);

    List<RemoteSettlement> queryDayBill(LocalDate billDate);
}
