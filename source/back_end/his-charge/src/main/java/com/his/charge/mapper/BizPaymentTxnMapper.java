package com.his.charge.mapper;

import com.his.charge.dto.PrepayQueryPageDTO;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.vo.PrepayVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 支付资金流水 Mapper。
 *
 * <p>账单是否付清由这里的 SUM 现算，不读任何状态列；{@code txn_status=2}（已冲正）的行
 * 永远排除在聚合之外，但行本身留在表里供追溯。
 */
@Mapper
public interface BizPaymentTxnMapper extends BaseMapper<BizPaymentTxn> {

    /**
     * 本账单净已收（收款为正、退款为负，一次 SUM 即净额）
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.bill_id = #{billId}
            """)
    BigDecimal sumNetByBill(@Param("billId") Long billId);

    /**
     * 本账单收款合计（正数）
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1 AND t.bill_id = #{billId}
            """)
    BigDecimal sumChargedByBill(@Param("billId") Long billId);

    /**
     * 本账单已退合计（返回正数表示的退款总额）
     */
    @Select("""
            SELECT IFNULL(SUM(-t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 2 AND t.bill_id = #{billId}
            """)
    BigDecimal sumRefundedByBill(@Param("billId") Long billId);

    @Select("""
            SELECT t.*
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.bill_id = #{billId}
            ORDER BY t.txn_time, t.id
            """)
    List<BizPaymentTxn> selectByBill(@Param("billId") Long billId);

    /**
     * 某笔收款流水还能退多少（原收款额 + 已退的负数合计）。
     * 原路退回必须退在具体的那一笔钱上，退超了渠道侧就会出现"本地退了 30、渠道只收过 20"。
     */
    @Select("""
            SELECT IFNULL((SELECT t.amount FROM biz_payment_txn t
                           WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.id = #{txnId}), 0)
                 + IFNULL((SELECT SUM(r.amount) FROM biz_payment_txn r
                           WHERE r.del_flag = 0 AND r.txn_status = 1 AND r.direction = 2
                             AND r.orig_txn_id = #{txnId}), 0)
            """)
    BigDecimal sumRefundableByTxn(@Param("txnId") Long txnId);

    // 住院预交金（bill_id IS NULL 的那一段）
    //
    // 集合条件必须是 `bill_id IS NULL AND source_type=3`，不能只按 direction 判：
    // 账单收退款同样落在本表，只按"这次住院 + 收钱"聚合会把出院结算的余额抵扣算成第二笔充值，
    // 预交金余额凭空翻倍（而它正是退款门禁的分母）。

    /**
     * 预交金流水分页（充值正、退款负；余额快照从配对的账户流水带出来）。
     *
     * <p>{@code prepayType} 直接输出 1/2 与 {@code direction} 同码值：充值=收款=1、退款=收款外=2，
     * 这里用 CASE 写死映射而不是"刚好相等"，否则将来 direction 加第三种方向就静默错位。
     */
    @Select("""
            SELECT t.id AS id,
                   t.txn_no AS prepayNo,
                   t.encounter_id AS admissionId,
                   t.patient_id AS patientId,
                   t.patient_no AS patientNo,
                   t.patient_name AS patientName,
                   CASE t.direction WHEN 1 THEN 1 ELSE 2 END AS prepayType,
                   t.amount AS amount,
                   a.balance_after AS balanceAfter,
                   t.pay_method AS payMethod,
                   t.receipt_no AS receiptNo,
                   t.txn_time AS payTime,
                   t.cashier_id AS operatorId,
                   t.cashier_name AS operatorName,
                   t.remark AS remark
            FROM biz_payment_txn t
            LEFT JOIN biz_fund_account_txn a ON a.del_flag = 0 AND a.payment_txn_id = t.id
            WHERE t.del_flag = 0 AND t.bill_id IS NULL AND t.source_type = 3
              AND (#{q.admissionId} IS NULL OR t.encounter_id = #{q.admissionId})
              AND (#{q.patientId} IS NULL OR t.patient_id = #{q.patientId})
              AND (#{q.prepayType} IS NULL
                   OR (#{q.prepayType} = 1 AND t.direction = 1)
                   OR (#{q.prepayType} = 2 AND t.direction = 2))
            ORDER BY t.txn_time DESC, t.id DESC
            """)
    IPage<PrepayVO> selectPrepayPage(IPage<PrepayVO> page, @Param("q") PrepayQueryPageDTO query);

    /**
     * 本次住院的预交金净额（充值 − 柜面退款，SQL 侧带符号 SUM；不含账单收退款）
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.bill_id IS NULL AND t.source_type = 3
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            """)
    BigDecimal sumPrepayNet(@Param("admissionId") Long admissionId);

    /**
     * 本次住院的预交金充值合计（正数）
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1
              AND t.bill_id IS NULL AND t.source_type = 3
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            """)
    BigDecimal sumPrepayRecharge(@Param("admissionId") Long admissionId);

    /**
     * 本次住院已从柜面退出去的预交金合计（返回正数表示的退款总额；出院退差转余额的不算"退现"）
     */
    @Select("""
            SELECT IFNULL(SUM(-t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 2
              AND t.bill_id IS NULL AND t.source_type = 3
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            """)
    BigDecimal sumPrepayRefunded(@Param("admissionId") Long admissionId);

    @Select("""
            SELECT COUNT(*)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.bill_id IS NULL AND t.source_type = 3
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            """)
    long countPrepay(@Param("admissionId") Long admissionId);

    /**
     * 出院结算退差合计（返回正数表示的搬走额）：这笔钱离开了这次住院、但没离开医院，
     * 所以它既不是柜面退款、也不该出现在收银员的点钞数里。
     */
    @Select("""
            SELECT IFNULL(SUM(-t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 2 AND t.source_type = 7
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            """)
    BigDecimal sumDischargeDiff(@Param("admissionId") Long admissionId);

    /**
     * 某次就诊在<b>账单上直接收</b>的钱（收正退负，净额）：欠费口径里的"患者又掏了一部分"。
     *
     * <p>必须排除余额抵扣（{@code pay_method=5}）：它花的正是预交金那笔钱，
     * 与"净预交"两边各算一次，等于同一笔钱计两遍收入，欠费榜会把欠 800 的人显示成不欠。
     * 预交金充值/退款本身没有账单锚（{@code bill_id IS NULL}），已被上面的条件天然排除。
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.bill_id IS NOT NULL
              AND t.pay_method <> 5
              AND t.encounter_type = #{encounterType} AND t.encounter_id = #{encounterId}
            """)
    BigDecimal sumDirectChargedByEncounter(@Param("encounterType") Integer encounterType,
                                           @Param("encounterId") Long encounterId);

    /**
     * 某张账单里"从住院账户抵扣"的那部分收款（余额支付的成功收款流水合计）。
     *
     * <p>不能拿 {@code paid_amount} 代替：出院结算那张账单往往既抵了预交金、又在柜面补了现金，
     * 两个数混在一起就说不清"患者口袋里又掏了多少"。
     */
    @Select("""
            SELECT IFNULL(SUM(t.amount), 0)
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1
              AND t.pay_method = 5 AND t.bill_id = #{billId}
            """)
    BigDecimal sumBalanceDeductByBill(@Param("billId") Long billId);

    /**
     * 本次住院可退的充值流水（FIFO 原路退回的对象）。
     *
     * <p>只取成功收款：已冲正的行不代表"这笔钱还在"，退它会出现本地退了、渠道只收过 0。
     */
    @Select("""
            SELECT t.*
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.txn_status = 1 AND t.direction = 1
              AND t.bill_id IS NULL AND t.source_type = 3
              AND t.encounter_type = 2 AND t.encounter_id = #{admissionId}
            ORDER BY t.txn_time, t.id
            """)
    List<BizPaymentTxn> selectPrepayDeposits(@Param("admissionId") Long admissionId);

    /**
     * 这些退费申请执行出去的退款流水（一页申请单一次 in 捞回来，别逐行查打成 N+1）。
     *
     * <p>一笔申请常常拆成多笔原路退回（现金 + 微信 + 余额），所以返回<b>全部</b>流水由调用方分组：
     * 只取第一笔会把"退出去多少钱"说小。
     */
    @Select("""
            <script>
            SELECT t.*
            FROM biz_payment_txn t
            WHERE t.del_flag = 0 AND t.direction = 2 AND t.txn_status = 1 AND t.apply_id IN
            <foreach item="id" collection="applyIds" open="(" separator="," close=")">#{id}</foreach>
            ORDER BY t.apply_id, t.id
            </script>
            """)
    List<BizPaymentTxn> selectRefundsByApplyIds(@Param("applyIds") List<Long> applyIds);
}
