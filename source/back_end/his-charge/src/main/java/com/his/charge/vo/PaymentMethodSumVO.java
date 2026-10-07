package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付方式分桶聚合（一个支付方式一行）。
 *
 * <p>对应两条同构 SQL：
 * <ul>
 *   <li>{@code BizDaySettlementMapper#sumPaidByPaymentMethod} —— 院级日结：当日全院按渠道分桶</li>
 *   <li>{@code BizCashierSettlementMapper#sumPaidBySettlement} —— 收费员班结：本班认领流水按渠道分桶</li>
 * </ul>
 *
 * <p>刻意<b>只返回原始分组、不在 SQL 里翻译成渠道名</b>：银行卡/转账在本层没有对应列，
 * 支付方式缺失也要单独归到「未知渠道」，这些是业务口径，由 Service 的分桶逻辑收口。
 * SQL 里一旦藏了这个判断，改口径的人就找不到该改哪儿。
 */
@Data
public class PaymentMethodSumVO implements Serializable {

    /**
     * 支付方式（{@code PaymentMethodEnum}）：
     * 1-现金 2-微信 3-支付宝 4-医保个账 5-院内余额 6-银行卡 7-转账；null 表示历史数据缺失。
     *
     * <p>6/7 与null 都由 Service 归入「未知渠道」，绝不并进微信/支付宝假装有数。
     */
    private Integer paymentMethod;

    /**
     * 该渠道的收款笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 该渠道的收款金额合计（元）
     */
    private BigDecimal amount;
}