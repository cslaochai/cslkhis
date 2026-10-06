package com.his.charge.service;

import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.BillRefundDTO;
import com.his.charge.dto.PaymentTxnQueryPageDTO;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.vo.BizPaymentTxnVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付资金流水服务（L3）：真金白银进出的唯一事实。
 *
 * <p><b>一笔钱一行、收退同表带符号</b>：{@code direction} 1-收 2-退，金额收正退负，
 * 日结 {@code SUM(amount)} 即净额。流水一旦写入，<b>资金事实</b>（金额、方向、渠道、状态）
 * 永不就地改写；收错钱了不就地删行，而是翻 txn_status=2-已冲正并另起一笔反向流水。
 * 账单是否付清由这里的 SUM 现算，不允许"点一下按钮翻支付状态"。
 * 唯一的例外是 {@link #claimForShift} 写的归集指针 —— 它登记"哪个班认领了这笔钱"，不碰钱。
 */
public interface PaymentService extends IService<BizPaymentTxn> {

    /**
     * 收款：一张账单可以一次提交多笔、多渠道（现金 + 余额 + 医保个账）。
     *
     * <p>每笔各自一行流水；余额支付还会在同一事务里扣账户，两条记录缺一条都对不上。
     */
    List<BizPaymentTxnVO> pay(BillPayDTO dto);

    /**
     * 按账单退费：红冲记账行（应收侧）+ 按原收款流水逐笔退回（资金侧），同一事务。
     *
     * <p>退款退在<b>具体哪一笔收款</b>上（{@code orig_txn_id}）：一账单多渠道组合支付时，
     * 只有原收款流水知道该退回去多少钱、走哪个渠道，退超了就是本地退了 30、渠道只收过 20。
     */
    List<BizPaymentTxnVO> refund(BillRefundDTO dto);

    /**
     * 整单撤销（退号、取消一次结算）：钱全额原路退回 + 本账单记账行全额红冲 + 账单作废。
     *
     * <p>与 {@link #refund} 的区别：退费是「按项退钱」，冲多少应收退多少钱，账单还开着；
     * 本方法是「这笔业务整个不要了」，退的钱按<b>净已收</b>算而不是按行金额算 ——
     * 部分支付的账单（欠 100 只收了 40）撤销时只退 40，但那 100 的应收要一并冲干净，
     * 否则号退完了收费台上还挂着一张永远收不到的账。
     *
     * <p>收过钱的账单最终也落在「4-已作废」：一进一出净额为 0，作废比「已退费」更能说明
     * 这张单子整体不存在了；而钱的事实永远在两笔流水里，不靠状态列表达。
     *
     * @return true=本次确实撤销；false=账单不存在或早已关闭（幂等跳过）
     */
    boolean closeBill(Long billId, String reason, Integer sourceType);

    PageResult<BizPaymentTxnVO> selectPage(PaymentTxnQueryPageDTO query);

    /**
     * 某张账单的全部收/退流水（详情与对账回显）
     */
    List<BizPaymentTxn> listByBill(Long billId);

    /**
     * 同上，出参换成 VO（HTTP 详情回显用；跨模块判账仍读 {@link #listByBill}）
     */
    List<BizPaymentTxnVO> listByBillVO(Long billId);

    /**
     * 收预交金：一行 {@code bill_id IS NULL} 的收款流水 + 一条住院账户入账流水，同一事务互指。
     *
     * <p><b>不建"0 元账单"去挂这笔钱</b>：预交金是医院欠患者的可退款项，不是"这批该收多少"的应收；
     * 造一张空账单会把 L2 与 L3 混成一层， {@code refreshFromTxns} 还会把它算成"应收 0、已收 3000"。
     * 代价是"这次住院收了多少钱"必须按 {@code encounter} 聚合，而不是顺着账单找 ——
     * 所以本表为此加了 {@code idx_txn_encounter}（sql/140）。
     */
    BizPaymentTxn prepayDeposit(PrepaySpec spec);

    /**
     * 退预交金（柜面取现/原路退回）：按原充值流水 FIFO 逐笔退回，一条流水退一笔钱。
     *
     * <p>退款金额先跟<b>账户余额</b>比，再动渠道：先做渠道请求才判余额的话，
     * 渠道已经把钱退出去了、本地却因为余额不足回滚，就成了"钱退了、账没冲"的长款。
     * 而 FIFO 摊到具体原流水（{@code orig_txn_id}）是必需的 —— 微信收的钱只能退回微信，
     * 拿柜面的现金去退它，现金抽屉当天就多出一笔谁也说不清的差额。
     */
    List<BizPaymentTxn> prepayRefund(PrepaySpec spec);

    /**
     * 出院结算退差：住院账户里剩下的预交金转入患者的院内余额账户（不动现金抽屉）。
     *
     * <p>表达成一笔 {@code source_type=7} 的退款流水 + 两条账户流水（住院账户扣、患者账户入）：
     * 钱确实离开了这次住院的预交金，但没有离开医院，所以它既不是收入也不该出现在点钞数里。
     * 患者要取现，再从院内余额走一次柜面退款即可 —— 那一步是真现金流出，会另起流水。
     */
    List<BizPaymentTxn> dischargeRemainder(PrepaySpec spec);

    /**
     * 交班归集：把某收银员<b>在交班时刻之前到达、且尚未被认领</b>的成功流水挂到这张交班单上。
     *
     * <p>只写 {@code cashier_settlement_id} 这一个归集指针，金额、方向、状态一列都不动 ——
     * "流水永不 UPDATE"禁的是销毁资金事实，不包括登记"这笔钱由哪个班认领"。
     * 有了它，日结找"未纳班结的钱"就是 {@code cashier_settlement_id IS NULL} 一次索引扫描，
     * 不必把全院流水和所有班次时段做笛卡尔比对。
     *
     * <p><b>区间只有上界，没有下界</b>：本方法是"这个班的行集"的唯一事实来源，班结快照与
     * 一级复算都按指针取数，不再用 {@code (period_begin, period_end]} 去猜。
     * 留下界会漏：流水时间与 {@code period_end} 都只截到秒，交班那一秒<b>后</b>到达的
     * 收款既没被本班统计过（聚合和归集都发生在它之前），又会被下一班的下界
     * {@code > period_begin}（= 上一次交班时刻）永久排除 —— 这笔钱就成了没人认领的孤儿，
     * 二级对账的"忘交班"差额再也归不了零，而它并不是有人忘交班。
     * 去掉下界后，凡是还没被认领的钱一律归这次交班，天然收敛，也顺带收回历史遗漏的孤儿流水。
     *
     * <p>收退两向都归集：交班单上既有点钞交出去的钱，也有从抽屉拿出去的退款，
     * 只归收款会让退款永远挂着"没交班"。
     *
     * @return 本次归集的流水笔数
     */
    int claimForShift(Long cashierId, LocalDateTime periodEnd, Long settlementId);

    /**
     * 住院预交金的一笔收/退（充值与退款共用同一份参数，方向由方法决定）。
     *
     * <p>患者信息必须由调用方带进来：L3 不认识"入院"这个临床概念，它只登记
     * 「这笔钱挂在哪个主体上」（{@code owner_type=2} + {@code ownerId=admissionId}）。
     *
     * @param amount       金额，一律正数；方向由调的方法决定，绝不允许传负数"表示退款"
     * @param payMethod    支付方式（字典 {@code his_pay_method}）；退款原路退回时以原充值流水为准
     * @param receiptNo    柜面纸质收据号，可与流水勾对（患者端自助充值无纸票，留空）
     * @param channelTxnNo 渠道真实交易号（患者端支付回调带回）；柜面扫码留空由服务端造模拟号
     * @param txnTime      交易时间（不传取当前；小程序回调要按回调时间落账，否则班结时段对不上）
     * @param remark       备注（退款写清原因）
     */
    record PrepaySpec(Long admissionId, Long patientId, String patientNo, String patientName,
                      BigDecimal amount, Integer payMethod, String receiptNo, String channelTxnNo,
                      LocalDateTime txnTime, String remark) implements Serializable {
    }
}
