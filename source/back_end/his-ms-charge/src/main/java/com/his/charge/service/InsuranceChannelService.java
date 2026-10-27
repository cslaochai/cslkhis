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

    /**
     * 2201 费用预结算（HIS 收费窗口实时调，医保平台算 splits，不落库）。
     *
     * <p>在收款弹层「医保个账」档点一下就调，返回统筹/个账/现金 splits 给收费员核对。
     * 与 {@link #checkInsured} 的区别：checkInsured 只查人在不在，这里连费用一起算。
     */
    SettleSplit preSettle(SettleRequest request);

    /**
     * 2301 门诊正式结算（HIS 收费窗口实时调，医保平台当场扣个账、出医保结算单号）。
     *
     * <p>收费员核对 splits 后点「确认医保结算」调一次；同一 tradeNo 幂等。
     * 成功后 HIS 侧只需再收 {@link SettleSplit#cashPay} 那段现金/扫码。
     */
    SettleSplit formalSettle(SettleRequest request);

    /**
     * 撤销医保侧实时结算（2305 冲正）：不确定态确认专用。
     *
     * <p>2301 超时等不确定失败后，请求可能已到医保侧扣了个账，HIS 不能无条件按失败降自费
     * （否则医保扣了 + 患者又付现金 = 双收）。发 2305 按 origTradeNo 冲正：
     * <ul>
     *   <li>回执成功 = 医保侧有单已冲正（个账已退回），此后降自费安全；</li>
     *   <li>回执「原上传流水不存在」= 医保侧根本没扣，降自费安全（按 errMsg 识别）；</li>
     *   <li>通道失败 = 医保侧状态仍未知，调用方必须保持不确定、禁止降级。</li>
     * </ul>
     */
    Receipt cancelRealtimeSettle(String origTradeNo, String tradeNo, String reason);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuredStatus implements Serializable {
        /**
         * 参保库是否查到此卡号
         */
        private boolean found;
        /**
         * 参保状态是否正常（1-正常参保）
         */
        private boolean active;
        /**
         * 险种（城镇职工医保/城乡居民医保/公费医疗）
         */
        private String insuranceType;
        /**
         * 参保人姓名
         */
        private String name;
        /**
         * 异常说明（未参保/停保/医保平台不可用时的原因）
         */
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

    /**
     * 实时结算请求（2201/2301 共用）。
     * tradeNo 由 HIS 侧生成（账单号+时间戳），2301 幂等键。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettleRequest implements Serializable {
        private String tradeNo;
        private String billNo;
        /**
         * 就诊类型：1-门诊 2-住院
         */
        private String encounterType;
        private String idCard;
        private String insuranceNo;
        private String patientName;
        private List<FeeItem> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeeItem implements Serializable {
        private String ybCode;
        private String name;
        /**
         * 目录支付类别：甲 / 乙 / 丙
         */
        private String payClass;
        /**
         * 乙类先行自付比例（0~1）
         */
        private BigDecimal selfRatio;
        private BigDecimal price;
        private Integer qty;
    }

    /**
     * 实时结算返回（2201/2301 共用）。
     * infcode=0 成功；2301 成功时 receiptNo 有值。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettleSplit implements Serializable {
        private Integer infcode;
        private String tradeNo;
        /**
         * 医保结算单号（YBZ+yyyyMMdd+序列），2301 成功时回
         */
        private String receiptNo;
        private String insuranceType;
        private String name;
        private BigDecimal total;
        /**
         * 统筹支付（医保基金付）
         */
        private BigDecimal insurancePay;
        /**
         * 个账支付（从参保人个人账户扣）
         */
        private BigDecimal personalAccount;
        /**
         * 现金支付（患者当场掏）
         */
        private BigDecimal cashPay;
        /**
         * 个人自付总额（= 个账 + 现金）
         */
        private BigDecimal selfPay;
        private BigDecimal deductible;
        /**
         * 个账扣后余额
         */
        private BigDecimal accountBalanceAfter;
        private String warnMsg;
        private String errMsg;
        /**
         * 失败是否「不确定」：true = 请求可能已到医保侧并扣账（超时/连接中断），调用方必须先冲正确认再降级；
         * false/null = 确定失败（请求未达或医保明确拒绝），可直接按失败处理。
         */
        private Boolean uncertain;
    }
}
