package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizCashierSettlement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 收费员交班单 Mapper。
 *
 * <p>跨表统计一律写在这里（不在 Service 里 selectList 再 stream 求和）：
 * 汇总要走数据库聚合，前端拿"当前页 list"去数就是只统计本页、翻页就变。
 *
 * <p><b>聚合源是支付资金流水（L3 支付流水），不是收费单状态列</b>：
 * 一个收银员一个班到底收进多少钱，唯一的事实是"他经手了几笔真金白银的进出"。
 * 一笔收几行费用、账单摊了几条明细都与点钞无关。口径三件事：
 * <ol>
 *   <li>{@code txn_status = 1}（成功）—— 已冲正（2）的流水不再是要点的钱；</li>
 *   <li>{@code direction} 1-收 2-退，收退分开口径（金额列收正退负，退费一律取绝对值），
 *       因为收费员面前既有"该交上去的钱"也有"该从抽屉拿出去的钱"；</li>
 *   <li><b>行集用归集指针 cashier_settlement_id = 本班ID，不用时段 {@code (begin, end]}
 *       现挑</b>。{@code period_begin/period_end} 只是凭证上的时间说明与滚动展示口径，
 *  不决定哪些流水算这个班的：时间与流水时间同为秒精度，"交班那一秒之后到达的收款"
 *   用时段挑必然两头落空（本班已聚合完、下一班的下界又把它排除在外），那笔钱会永久没人认领。
 *   交班时由 {@code claimForShift} 把未认领的流水一次登记给本班，此后班结快照与
 *   一级对账复算读的是同一个集合 —— 左右两侧不再是"各自按时间猜一遍"。</li>
 * </ol>
 *
 * <p><b>交班单没有"统筹"这一项</b>：收费员班结单的统筹金额是医保局后付给
 * 医院的钱，收银员既不经手也不点钞，硬塞进班结会让现金清点凭空多出一块说不来的差额。
 * 它只在院级日结出现（见 {@link BizDaySettlementMapper#sumPoolAmount}）。
 */
@Mapper
public interface BizCashierSettlementMapper extends BaseMapper<BizCashierSettlement> {

    /**
     * 某收费员上一次交班的结束时刻（滚动区间的起点）。
     *
     * <p>用 {@code MAX(period_end)} 而不是"最近一条记录"：取最大值可以免疫乱序插入。
     * 查不到（从未交班）返回 null，由调用方回落到当日 00:00:00。
     */
    @Select("SELECT MAX(period_end) FROM biz_cashier_settlement " +
            " WHERE cashier_id = #{cashierId} AND del_flag = 0")
    LocalDateTime selectLastPeriodEnd(@Param("cashierId") Long cashierId);

    /**
     * 本班认领的收款流水按支付方式汇总（交班时的系统账 / 一级对账的复算右值）。
     *
     * <p>返回原始分组，"无对应渠道列的支付方式"（银行卡/转账）与方式缺失的判定交给
     * Service 的 {@code unknownPay} 分桶 —— SQL 里不藏业务口径。
     */
    @Select("SELECT pay_method AS paymentMethod, COUNT(*) AS cnt, " +
            "       COALESCE(SUM(amount), 0) AS amount " +
            " FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 1 " +
            "   AND cashier_settlement_id = #{settlementId} " +
            " GROUP BY pay_method")
    List<Map<String, Object>> sumPaidBySettlement(@Param("settlementId") Long settlementId);

    /**
     * 本班认领的退费（掏出去的钱）。
     *
     * <p>{@code ABS}：流水金额收正退负，班结单上的退费金额与前端展示都用正数，
     * 符号只留在 net_amount = 收 - 退这一步算。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(ABS(amount)), 0) AS amount " +
            " FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 2 " +
            "   AND cashier_settlement_id = #{settlementId}")
    Map<String, Object> sumRefundBySettlement(@Param("settlementId") Long settlementId);

    /**
     * 本班认领账单的开票 / 作废张数。
     *
     * <p>口径是"票跟着账单走"：{@code invoice.bill_id ∈} 本班<b>成功收款</b>涉及的账单，
     * 而不是"开票时间落在时段内" —— 后者会把"昨天收钱今天补打票"漏掉，
     * 收费员手里的票根数就跟当天收的钱对不上。
     *
     * <p>作废数把 3-已作废与 4-已红冲换开都算上：红冲换开的原票同样是
     * 一张不能再交出去的废票，只数 3 会让废票根悄悄消失。
     */
    @Select("SELECT COUNT(*) AS cnt, " +
            "       COALESCE(SUM(i.invoice_status IN (3, 4)), 0) AS voidCnt " +
            " FROM biz_invoice i " +
            " WHERE i.del_flag = 0 AND i.bill_id IS NOT NULL " +
            "   AND i.bill_id IN (SELECT DISTINCT t.bill_id FROM biz_payment_txn t " +
            "                      WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1 " +
            "                        AND t.cashier_settlement_id = #{settlementId})")
    Map<String, Object> sumInvoiceBySettlement(@Param("settlementId") Long settlementId);

    /**
     * 当日已用交班单序号数（生成 JS+日期+序号用）。
     */
    @Select("SELECT COUNT(*) FROM biz_cashier_settlement WHERE settlement_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);
}
