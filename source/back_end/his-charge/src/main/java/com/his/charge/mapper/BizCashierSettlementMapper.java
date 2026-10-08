package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizCashierSettlement;
import com.his.charge.vo.CountAmountVO;
import com.his.charge.vo.InvoiceCountVO;
import com.his.charge.vo.PaymentMethodSumVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收费员交班单 Mapper。
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
    List<PaymentMethodSumVO> sumPaidBySettlement(@Param("settlementId") Long settlementId);

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
    CountAmountVO sumRefundBySettlement(@Param("settlementId") Long settlementId);

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
    InvoiceCountVO sumInvoiceBySettlement(@Param("settlementId") Long settlementId);

}
