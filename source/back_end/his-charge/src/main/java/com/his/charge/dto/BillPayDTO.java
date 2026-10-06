package com.his.charge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账单收款入参（L3）：一次提交可以带多笔、多渠道（现金 30 + 余额 50 + 医保个账 20）。
 *
 * <p>一笔钱一行流水，绝不允许"合并成一笔收款"：合并后既表达不了组合支付，
 * 退费时无从知道该退回哪个渠道，日结也无从按渠道清点。
 */
@Data
public class BillPayDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;

    /**
     * 流水来源（字典 his_txn_source）：为空按 1-收费台收款。
     * 患者端回调等内部调用方必须显式写自己的来源，否则班结会把自助缴费算到收银员头上。
     */
    private Integer sourceType;

    /**
     * 明细项集合
     */
    @NotEmpty(message = "缺少收款明细")
    @Valid
    private List<PayItem> items;

    /**
     * 一笔收款：支付方式（字典 his_pay_method）、金额（必须大于 0）、渠道流水号
     * （患者端回调回填真实号，柜面扫码留空由服务端造模拟号）、
     * 账户主体ID（pay_method=5 必填：门诊=患者ID，住院=入院ID）。
     */
    @Data
    public static class PayItem {

        /**
         * 支付方式（1-现金 2-微信 3-支付宝 4-医保个人账户 5-院内余额 6-银行卡 7-转账）
         */
        @NotNull(message = "缺少支付方式")
        private Integer payMethod;

        @NotNull(message = "缺少收款金额")
        @DecimalMin(value = "0.01", message = "收款金额必须大于 0")
        private BigDecimal amount;

        /**
         * 渠道流水号
         */
        private String channelTxnNo;

        private Long ownerId;

        /**
         * 备注
         */
        private String remark;
    }
}
