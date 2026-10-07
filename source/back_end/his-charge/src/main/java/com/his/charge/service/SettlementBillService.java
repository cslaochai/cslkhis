package com.his.charge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.charge.dto.BillQueryPageDTO;
import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.charge.dto.BillVoidDTO;
import com.his.charge.dto.PendingEncounterQueryPageDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.vo.*;
import com.his.common.base.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 结算账单服务（L2）：把一批记账行锁成"这笔该收多少、怎么分"。
 *
 * <p>账单只回答<b>应收与分摊</b>，不回答钱到没到：{@code paid_amount} 是支付流水的冗余镜像，
 * 权威永远在支付资金流水，每次流水后由 {@link #refreshFromTxns} 重算。
 * 优惠与医保 split 分列（{@code discount_amount} 只放院内优惠/抹零，统筹走统筹金额），
 * 混写一列等于让真优惠无处安放、现金清点多出一块说不清的差额。
 */
public interface SettlementBillService extends IService<BizSettlementBill> {

    /**
     * 结算出账：选中的待结算记账行 → 账单 + 账单行快照 + 记账行锁定。
     *
     * <p>账单金额一律服务端现算（应收=Σ记账行净额、split 按目录类别算），
     * 入参只能决定"哪些行进这张账单"和"优惠多少"。
     */
    BizSettlementBill settle(BillSettleUpsertDTO dto);

    /**
     * 结算出账并回显账单：与 {@link #settle} 同一事务，只是出参换成 VO。
     */
    BizSettlementBillVO settleVO(BillSettleUpsertDTO dto);

    /**
     * 结算试算：与 {@link #settle} 走同一套计算（同一份草稿），只是不落库、不锁行。
     *
     * <p>开这个口子是为了消灭"住院结算自己再算一遍"：原先出院试算逐单调旧收费侧的试算再汇总，
     * 出账时 L2 又算一遍统筹/自付，两套数一漂移，小票就跟结算单对不上。
     */
    BillPreviewVO previewSettlement(BillSettleUpsertDTO dto);

    /**
     * 取消结算：账单作废 + 记账行解锁。<b>已有收款的账单不许作废</b>，必须走退费。
     */
    void voidBill(BillVoidDTO dto);

    /**
     * 建立红冲链：新账单重结时指向被冲的原账单（结算账单.orig_bill_id）。
     *
     * <p>调用时机：voidBill(旧账单) → settle(生成新账单) → linkRedoChain(新账单ID, 旧账单ID)。
     */
    void linkRedoChain(Long newBillId, Long origBillId);

    /**
     * 按支付流水重算账单的已收/已退与状态（每笔收退款后调用）。
     */
    BizSettlementBill refreshFromTxns(Long billId);

    /**
     * 某次就诊下还有多少钱没结清（临床门禁的唯一口径：签到、发药、执行问的是这里，
     * 绝不问收费单状态列 —— 否则免收和 0 元单都得去支付层造假数据）。
     */
    BigDecimal unpaidAmount(Integer encounterType, Long encounterId);

    /**
     * 某次就诊下的全部账单（收费台与住院站回显用）
     */
    List<BizSettlementBill> listByEncounter(Integer encounterType, Long encounterId);

    /**
     * 结算台候选：某次就诊下待结算的记账行 + 本次合计 + 已出账单未收讫差额。
     *
     * <p>就诊标识不合法时抛业务异常而不是查全表 —— 少了 encounterId 的"待结算"会把全院费用算成一次就诊的。
     */
    PendingFeeVO pendingFees(Integer encounterType, Long encounterId);

    /**
     * 本次住院最后一张<b>未作废</b>的出院结算账单（{@code bill_type=4}）；没有返回 {@code null}。
     *
     * <p>"什么算已结算"必须只有一个答案：出院门禁、结算回显、账务概览都读这里，
     * 各自写一份筛选就会漂（作废单到底算不算结算，两处判断不一样的那天就出事故）。
     */
    BizSettlementBill latestDischargeBill(Long admissionId);

    PageResult<BizSettlementBillVO> selectPage(BillQueryPageDTO query);

    /**
     * 患者端待缴账单列表（含逐条明细）：该患者的未付清账单（1-待支付 / 2-部分支付）按出账倒序。
     *
     * <p>四层改造后患者缴费从旧的「收费单」切到「结算账单」，本方法是小程序门诊缴费入口的列表数据源，
     * 与 {@code refundableLines} 同口径只回答"该收多少、每行是什么"，不回答钱到没到（已收以支付流水为准）。
     *
     * <p>账单头 + 摊行明细 + 医保拆分三列都在 VO 上，取值不需要再靠 {@code Map} 的 key 名去猜字段。
     */
    List<PendingBillVO> pendingBillViews(Long patientId);

    /**
     * 收费台首屏：按<b>就诊</b>汇总的待收费榜（待出账的应收 + 未收齐的账单差额，两列分开）。
     *
     * <p>旧收费台列的是「收费单」，于是窗口只能看见已经开出来的单，看不见
     * 「医生开了病历、还没人结算」的那一批 —— 那一半活儿在四层里恰恰是最先要干的。
     */
    PageResult<PendingEncounterVO> pendingEncounterPage(PendingEncounterQueryPageDTO query);

    BizSettlementBillDetailVO getDetailById(Long billId);

    /**
     * 退费候选清单：账单下某一退费类型还能<b>整条</b>退的记账行（按出账顺序）。
     *
     * <p>退费申请单没有"勾哪几行"这个旋钮，只有类型与金额，所以发起与执行都要拿这份清单
     * 判「金额落不落得到明细边界」。判据必须与 {@code PaymentService.refund} 同源，
     * 否则会出现窗口说能退、收费处点执行才被打回。
     */
    /**
     * 患者维度账单明细（医生站 / 今日就诊回显「患者已收费项目」用）：返回该患者全部账单行快照。
     *
     * <p>四层一个患者多张账单，旧两层是「一个患者一条收费单」；这里返回扁平的账单行列表，
     * 由前端按 itemType 分组、按 encounterId 关联挂号。账单行已是 L2 快照，
     * 项目名 / 价格 / 医保拆分不随字典与价格变更而漂移。
     */
    List<BizSettlementBillItemVO> listItemsByPatient(Long patientId);

    List<RefundableLineVO> refundableLines(Long billId, Integer refundType);
}
