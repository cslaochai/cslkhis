package com.his.emr.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 来源单据的缴费推进：收费四层把钱收齐后，把处方 / 检查申请 / 检验申请推到「已缴费」。
 *
 * <p><b>为什么单独一个服务而不是让收费侧直接改这些表</b>：处方、申请单、发药记录的主档都在本模块，
 * 收费只负责说「这笔钱到账了」。原来 {@code ChargeController} 直接注入 his-emr 的四个 mapper 自己写，
 * 于是「明细缴了、主表没缴」「发药记录重复插」这类联动缺口全留在收费代码里，没人能一眼看出漏了哪步。
 *
 * <p><b>推进与撤销的容错口径是反的</b>（这是本服务最重要的一条）：
 * <ul>
 *   <li>{@code advance*} 在收款的同一事务里跑，单据找不到就抛错 —— 钱不能收进一张不存在的处方；</li>
 *   <li>{@code revert*} 在退款的同一事务里跑，一律<b>找不到就跳过</b> —— 患者的钱已经退了，
 *       不能因为一张历史单据被物理清了就把整笔退款回滚掉。</li>
 * </ul>
 */
public interface SourcePaidAdvanceService {

    /**
     * 处方明细缴清：明细置已缴费 → 该处方全部明细缴清后主表置已缴费 → 为本条明细生成待发药记录。
     *
     * <p>幂等：重复推进不会产生第二张发药记录（发药窗口按 prescription_detail_id 判重）。
     *
     * @param paidAmount 本次这张账单里该明细对应的实缴金额（写入处方主表 pay_amount 用合计，不用单行）
     * @param payMethod  主要支付渠道（组合支付时取金额最大的一笔；0 元账单可为空）
     */
    void advancePrescriptionDetail(Long prescriptionDetailId, BigDecimal paidAmount, Integer payMethod);

    /**
     * 处方明细整行红冲（退费）：明细置已退费、未发的发药记录取消，全部退费时主表置已退费
     */
    void revertPrescriptionDetail(Long prescriptionDetailId, String reason);

    /**
     * 检查申请单缴清：置「2-已缴费」（执行记录由医技域另建，不在这里）
     */
    void advanceInspectionApply(Long applyId);

    /**
     * 检查申请单退回「1-已提交」（已取消的单子不动）
     */
    void revertInspectionApply(Long applyId);

    /**
     * 检验申请单缴清
     */
    void advanceLaboratoryApply(Long applyId);

    /**
     * 检验申请单退回「1-已提交」
     */
    void revertLaboratoryApply(Long applyId);

    /**
     * 药品退费闸（sql/154）：这些处方明细里只要还有「已发药、没办退药」的记录，就抛业务异常。
     *
     * <p>为什么必须在<b>动钱之前</b>问而不是退完再补：{@code revertPrescriptionDetail} 对已发药的行
     * 是「跳过不回库」的（回库是药师的退药动作，要实物验收），所以钱先退了药还留在患者手里，
     * 库存账实永久不符，而且再也找不出是谁退的这笔钱。
     * <br>本方法与 {@code revert*} 的容错口径相反：它是闸门，查不到就放行，查到就当场拒绝。
     *
     * @param scene 业务场景文字（进报错文案，让前台知道是哪一步被挡的）
     */
    void assertNoDrugPendingReturn(List<Long> prescriptionDetailIds, String scene);
}
