package com.his.charge.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InsuranceChannelService {

    Receipt send(OutboundMessage message);

    List<RemoteSettlement> queryDayBill(LocalDate billDate);

    /**
     * 参保核验：按医保卡号查医保局端参保状态（对齐真实 HIS 的联网参保查询）。
     * 未参保登记 / 停保 / 待遇状态异常 → found=false 或 active=false，结算前据此拒医保结算。
     */
    InsuredStatus checkInsured(String insuranceNo);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuredStatus implements Serializable {
        /** 参保库是否查到此卡号 */
        private boolean found;
        /** 参保状态是否正常（1-正常参保） */
        private boolean active;
        /** 险种（城镇职工医保/城乡居民医保/公费医疗） */
        private String insuranceType;
        /** 参保人姓名 */
        private String name;
        /** 异常说明（未参保/停保/医保平台不可用时的原因） */
        private String errMsg;
    }

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
}
