package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 票据张数聚合（开票数 + 作废数），对应两条同构 SQL：
 * <ul>
 *   <li>{@code BizDaySettlementMapper#sumInvoice} —— 当日收讫账单开出的票</li>
 *   <li>{@code BizCashierSettlementMapper#sumInvoiceBySettlement} —— 本班认领账单开出的票</li>
 * </ul>
 *
 * <p>口径都是「票跟着账单走」而不是「按开票时间」—— 否则"昨天收钱今天补打票"
 * 会算到今天，跟当天收的钱对不上。
 *
 * <p><b>{@code voidCnt} 的类型确认</b>：SQL 写的是
 * {@code COALESCE(SUM(i.invoice_status IN (3, 4)), 0)}，MySQL 对布尔表达式求和返回
 * DECIMAL，而 {@code cnt} 是 COUNT 的 BIGINT。两者都映射成 {@link Long} ——
 * MyBatis 用 {@code ResultSet.getLong()} 取值，对 DECIMAL 列同样安全（会做值转换而非截断），
 * 且作废张数是计数而非金额，不存在精度风险。
 */
@Data
public class InvoiceCountVO implements Serializable {

    /**
     * 票据总张数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 作废张数（含 3-已作废与 4-已红冲换开，两者都是交不出去的废票根）
     */
    private Long voidCnt;
}