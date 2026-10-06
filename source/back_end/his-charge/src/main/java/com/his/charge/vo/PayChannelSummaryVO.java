package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 渠道对账汇总（本地口径 vs 渠道口径，逐渠道同键比较）
 *
 * <p>两侧同为<b>带符号净额</b>：本地取支付资金流水当日该渠道成功流水
 * （收款正、退款负），渠道侧取台账流水金额。退款日差额为负是正常的，
 * 不再像旧口径那样只数"已收费单"，否则渠道退了钱本地永远对不上。
 */
@Data
public class PayChannelSummaryVO {

    /**
     * 账单日期 yyyy-MM-dd
     */
    private String billDate;

    private List<Row> rows;

    @Data
    public static class Row {
        /**
         * 支付渠道：2-微信 3-支付宝 6-银行卡
         */
        private Integer channel;
        /**
         * 渠道文案（服务端出，前端不再猜）
         */
        private String channelText;

        /**
         * 本地口径：当日该渠道成功支付流水笔数
         */
        private Long localCount;
        /**
         * 本地口径：净收合计（收正退负 SUM）
         */
        private BigDecimal localAmount;

        /**
         * 渠道口径：流水笔数（全部状态）
         */
        private Long flowTotal;
        /**
         * 渠道口径：已勾对笔数
         */
        private Long matchedCount;
        /**
         * 渠道口径：已勾对金额
         */
        private BigDecimal matchedAmount;
        /**
         * 渠道口径：待勾对笔数
         */
        private Long unmatchedCount;
        /**
         * 渠道口径：待勾对金额（含长款/短款未定性的钱）
         */
        private BigDecimal unmatchedAmount;

        /**
         * 差额 = 渠道侧总额 - 本地总额（正数 = 渠道多收，长款嫌疑）
         */
        private BigDecimal diffAmount;
    }
}
