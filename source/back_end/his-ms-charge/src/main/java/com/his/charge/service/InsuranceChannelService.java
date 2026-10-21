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
     * 参保核验：向医保局端查参保状态（对齐真实 HIS 的联网参保查询）。
     *
     * <p>只在<b>结算那一刻</b>调用一次 —— 挂号不查、登记不查，避免患者还没走到付费环节
     * 就被参保状态挡住。新增挂号只登记，缴不缴得起、报不报得了，付款时才见分晓。
     *
     * <p>实名核验口径：入参同时带患者主档的身份证/姓名用于防冒名。
     * <ul>
     *   <li>主档有身份证 → 身份证是实名金标准，按身份证查参保（查无即未参保）；</li>
     *   <li>主档无身份证 → 按卡号查参保，命中后比对姓名，不一致判「身份不符」。</li>
     * </ul>
     * mock 通道没有医保平台可查，恒返回「查无参保」。
     *
     * <p>查无参保 / 停保 / 身份不符 / 平台不可用 → found=false 或 active=false，
     * 结算侧据此<b>静默按自费原价收</b>，不拦单（见 SettlementBillServiceImpl#resolveInsurance）。
     */
    InsuredStatus checkInsured(String insuranceNo, String idCard, String name);

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
