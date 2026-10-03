package com.his.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.entity.BizDaySettlement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 院级日结单 Mapper。
 *
 * <p><b>三条互相独立的取证链，这正是三级对账能出差异的原因</b>：
 * <ol>
 *   <li><b>资金链（L3 支付资金流水）</b>：当日全院真金白银的进出。
 *       这是"钱到底有多少"的唯一事实，日结单的收退款额、支付渠道分桶、账单数全部现算自这里。</li>
 *   <li><b>凭证链（L4 收费员班结单）</b>：各班结单在交班那一刻定格的金额之和
 *       （Σ交班单）。它由收银员逐班确认，与全院流水一比就知道<b>谁没交班</b>。</li>
 *   <li><b>账单链（L2 结算账单 + {@code _item}）</b>：当日<b>收讫</b>账单的
 *       单头应收合计 ↔ 明细摊行合计，以及按科室归集的收入。两表各存一份金额，
 *       对得上才说明"摊行没漏没重"。</li>
 * </ol>
 * 如果二级、三级共用同一个数据源，那"对上了"只证明自己等于自己，所以每条聚合都写清勾的是哪条链。
 *
 * <p>⚠ <b>三点口径，全文件统一，改一处必须一起改</b>：
 * <ol>
 *   <li><b>时间区间一律左开右闭 {@code (begin, end]}</b>。交班单也是这个口径
 *       （{@code period_begin} = 上次交班时刻，那一秒已被上次统计过）。
 *       全库时间统一截断到秒，所以日结的"整日"用
 *       (前一日 23:59:59, 当日 23:59:59] 表示，不会漏掉 00:00:00 那一秒。</li>
 *   <li><b>流水只认 {@code txn_status = 1}（成功）</b>，收退靠 {@code direction} 分列而不是靠符号猜；
 *       金额列收正退负，对外展示的退费金额一律 {@code ABS} 取正，符号只留在净额那一步。</li>
 *   <li><b>账单链的"当日"用支付时间（收讫时刻）而不是账单日期（出账日）</b>：
 *       一张昨天出账、今天补齐的钱属于今天的日结。作废单（4）永远没有 pay_time，
 *       部分支付（2）尚未收讫 —— 两者的钱都还没落定，不进账单链统计；
 *       但部分支付<b>已收的那笔流水</b>仍在资金链里，这类"账单未平而钱已进"是二级对账的正常差额，
 *       由 {@code unassigned} 与 Σ交班单那一级显式暴露，不靠改口径抹平。</li>
 * </ol>
 */
@Mapper
public interface BizDaySettlementMapper extends BaseMapper<BizDaySettlement> {

    // 资金链：支付资金流水

    /**
     * 当日全院实收合计（方向=收、成功）。
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 1 " +
            "   AND txn_time > #{begin} AND txn_time <= #{end}")
    BigDecimal sumPaidAmount(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 当日全院实收笔数（一笔流水一条，不等于"几笔收费业务"）。
     */
    @Select("SELECT COUNT(*) FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 1 " +
            "   AND txn_time > #{begin} AND txn_time <= #{end}")
    long countPaid(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 当日全院退款合计（方向=退、成功；取绝对值，对外是正数）。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(ABS(amount)), 0) AS amount " +
            " FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 2 " +
            "   AND txn_time > #{begin} AND txn_time <= #{end}")
    Map<String, Object> sumRefund(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 当日按支付方式的收款分桶（渠道口径与交班单同源，才能互相复核）。
     */
    @Select("SELECT pay_method AS paymentMethod, COUNT(*) AS cnt, " +
            "       COALESCE(SUM(amount), 0) AS amount " +
            " FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 1 " +
            "   AND txn_time > #{begin} AND txn_time <= #{end} " +
            " GROUP BY pay_method")
    List<Map<String, Object>> sumPaidByPaymentMethod(@Param("begin") LocalDateTime begin,
                                                     @Param("end") LocalDateTime end);

    /**
     * 当日发生过收款的账单数（去重 bill_id）。
     *
     * <p>一层模型时代没有这个数，只能数收费单据；四层后一笔业务可能对应多张账单、
     * 一张账单可能多笔流水，"日结覆盖了多少张账单"才是能拿去跟前台交单量核对的口径。
     */
    @Select("SELECT COUNT(DISTINCT bill_id) FROM biz_payment_txn " +
            " WHERE del_flag = 0 AND txn_status = 1 AND direction = 1 " +
            "   AND txn_time > #{begin} AND txn_time <= #{end}")
    long countPaidBills(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 未纳入任何交班单的收款流水（收了钱没人交班 / 交完班又收钱的）。
     *
     * <p>判据是交班时写下的归集指针 {@code cashier_settlement_id IS NULL}，而不是
     * "把流水和所有班次时段做一次笛卡尔比对"：指针是<b>已经发生过的登记事实</b>，
     * 时段比对是<b>现算的推测</b>。用推测做判据会出这种情况 —— 有人补录了一张历史时段的
     * 交班单，一大片本来明明没人交班的流水就突然"被认领"了，差额凭空消失。
     *
     * <p>这是<b>二级对账最会漏的一类</b>：只看"Σ班结 = Σ班结"永远平，
     * 只有把没人认领的流水单独捞出来，"忘交班"才会变成一个金额。
     *
     * <p>{@code cashier_id > 0} 把系统代收（患者端自助缴费）排在本查询外 ——
     * 那笔钱没有收银员会为它交班，混进来会让"忘交班"这个差额永远非零、
     * 从而再也没人认真看它。它由 {@link #sumSystemCollected} 单列，并交给渠道对账单核对。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(t.amount), 0) AS amount " +
            " FROM biz_payment_txn t " +
            " WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1 " +
            "   AND t.txn_time > #{begin} AND t.txn_time <= #{end} " +
            "   AND t.cashier_settlement_id IS NULL AND t.cashier_id > 0")
    Map<String, Object> sumUnassigned(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 系统代收的收款流水（{@code cashier_id = 0}：患者端自助缴费、后台任务）。
     *
     * <p>它不进二级对账的差额（没人欠它一次交班），但必须显式报出来：
     * "当日实收里有这么一笔钱是机器收的，要拿去和微信/支付宝渠道账单勾对"。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(t.amount), 0) AS amount " +
            " FROM biz_payment_txn t " +
            " WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1 " +
            "   AND t.txn_time > #{begin} AND t.txn_time <= #{end} AND t.cashier_id = 0")
    Map<String, Object> sumSystemCollected(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    // 账单链：结算账单 + 结算账单行
    // 集合条件统一：当日收讫（pay_time 落在区间内）且状态为 3-已支付 / 5-已退费

    /**
     * 医保统筹记账额（当日收讫账单的统筹金额合计）。
     *
     * <p><b>统筹只出现在日结，绝不出现在交班单</b>：它是医保局后付给医院的钱，
     * 收银员既不经手也不点钞，塞进班结会让现金清点凭空多出一块说不清的差额。
     * 单列出来是给医保报盘台账核对用的，不进 {@code net_amount}。
     *
     * <p>按"账单收讫"计而不是按"流水"计，是因为统筹天然一次结清：账单没付清（部分支付）
     * 说明统筹还没实现，此时记账会把未发生的钱算进当日收入。
     */
    @Select("SELECT COALESCE(SUM(pool_amount), 0) FROM biz_settlement_bill " +
            " WHERE del_flag = 0 AND bill_status IN (3, 5) " +
            "   AND pay_time > #{begin} AND pay_time <= #{end}")
    BigDecimal sumPoolAmount(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 当日收讫账单的单头应收合计（三级对账右值）。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(total_amount), 0) AS amount " +
            " FROM biz_settlement_bill " +
            " WHERE del_flag = 0 AND bill_status IN (3, 5) " +
            "   AND pay_time > #{begin} AND pay_time <= #{end}")
    Map<String, Object> sumBillHeader(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 按科室归集明细摊行金额（毛收入，未扣优惠/统筹）。
     *
     * <p>科室锚点取的是摊行自己的科室ID（出账时从记账行带过来），
     * 因此这张表按"哪科室开的费用"计，与谁收的钱无关。
     */
    @Select("SELECT i.dept_id AS deptId, MAX(i.dept_name) AS deptName, " +
            "       COUNT(*) AS cnt, COALESCE(SUM(i.amount), 0) AS amount " +
            " FROM biz_settlement_bill_item i " +
            " JOIN biz_settlement_bill b ON b.id = i.bill_id " +
            " WHERE i.del_flag = 0 AND b.del_flag = 0 AND b.bill_status IN (3, 5) " +
            "   AND b.pay_time > #{begin} AND b.pay_time <= #{end} " +
            "   AND i.dept_id IS NOT NULL " +
            " GROUP BY i.dept_id " +
            " ORDER BY amount DESC")
    List<Map<String, Object>> sumDetailByDept(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 无科室归属的摊行（科室对账的差异项，单列）。
     *
     * <p>新数据应当 100% 有归属；这里非零就是"某类费用开单时没带科室"，
     * 是记账侧的缺陷，绝不能并进任何科室统计。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(i.amount), 0) AS amount " +
            " FROM biz_settlement_bill_item i " +
            " JOIN biz_settlement_bill b ON b.id = i.bill_id " +
            " WHERE i.del_flag = 0 AND b.del_flag = 0 AND b.bill_status IN (3, 5) " +
            "   AND b.pay_time > #{begin} AND b.pay_time <= #{end} " +
            "   AND i.dept_id IS NULL")
    Map<String, Object> sumDetailUnattributed(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 全部摊行金额（有归属 + 无归属），用于验证"科室归集没漏也没多"。
     */
    @Select("SELECT COUNT(*) AS cnt, COALESCE(SUM(i.amount), 0) AS amount " +
            " FROM biz_settlement_bill_item i " +
            " JOIN biz_settlement_bill b ON b.id = i.bill_id " +
            " WHERE i.del_flag = 0 AND b.del_flag = 0 AND b.bill_status IN (3, 5) " +
            "   AND b.pay_time > #{begin} AND b.pay_time <= #{end}")
    Map<String, Object> sumDetailAll(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    /**
     * 当日收讫账单开出的票据张数 / 作废张数。
     *
     * <p>口径是"票跟着账单走"（按账单收讫日归集），不是按开票时间 ——
     * 否则"昨天收费今天补打票"会被算进今天的票据数，跟当天收费对不上。
     * 作废含 4-已红冲换开的原票（它同样是一张交不出去的废票根）。
     */
    @Select("SELECT COUNT(*) AS cnt, " +
            "       COALESCE(SUM(i.invoice_status IN (3, 4)), 0) AS voidCnt " +
            " FROM biz_invoice i " +
            " JOIN biz_settlement_bill b ON b.id = i.bill_id " +
            " WHERE i.del_flag = 0 AND b.del_flag = 0 AND b.bill_status IN (3, 5) " +
            "   AND b.pay_time > #{begin} AND b.pay_time <= #{end}")
    Map<String, Object> sumInvoice(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
