package com.his.charge.service;


import com.his.charge.entity.BizFeeRecord;
import com.his.charge.entity.BizSettlementBill;
import java.util.List;

/**
 * 来源单据推进（L2 结清后把临床单据往前推一步）。
 *
 * <p><b>为什么由收费层触发而不是开单时直接推</b>：处方/申请单上的「已缴费」表达的是
 * 患者钱已掏，属于 L3 的事实；而"钱是否收齐"只有账单结清那一刻才有答案
 * （组合支付、部分收款、欠费结算都会让它中途不成立）。所以推进点只有一个：
 * 账单从非已支付转为 3-已支付。
 *
 * <p><b>反向动作同样必须成对</b>：退费把钱退了，处方不能继续挂着「已缴费」让药房发药，
 * 申请单也不能停在「已缴费」让医技执行。撤销的触发条件是记账行被<b>整行红冲</b>
 * （部分冲减的行仍在发生，不能撤）。
 *
 * <p>本服务只负责「按记账行的来源单据分派」，真正改哪张表由单据所属模块自己做
 * （见 his-emr 的 {@code SourcePaidAdvanceService}、his-medicaltech 的执行记录），
 * 收费侧不再跨模块直接写别人的表。
 */
public interface SourceAdvanceService {

    /**
     * 账单结清 → 推进其名下记账行的来源单据（处方 / 检查申请 / 检验申请）。
     *
     * <p>幂等由下游保证：同一账单重复推进不会产生第二张待发药记录、也不会把申请单刷两次。
     */
    void advanceByBill(BizSettlementBill bill);

    /**
     * 账单退费 → 把被<b>整行红冲</b>的记账行对应的来源单据退回未缴费。
     *
     * <p>由支付层在退款流水写完后调用，与退款同事务；任何一张单据找不到都只记日志，
     * 不能让退款流水回滚（钱必须退掉）。
     */
    void revertByBill(Long billId, String reason);

    /**
     * 同一件事的「行清单已经在我手上」版本：整单撤销时账单一作废就清空账单ID，
     * 那时按账单再也捞不出这些行，只能在撤销前把行捞出来传进来。
     */
    void revertByRows(List<BizFeeRecord> rows, String reason);

    /**
     * 药品退费闸（sql/154）：本批记账行里凡来自处方的，先确认它名下的药「没发出去」或「已经退药」，
     * 否则当场拒绝退费。
     *
     * <p>为什么放在收费层而不是让发药模块自己管：判断"这笔钱能不能退"要同时看记账行的来源锚点
     * （source_type=2 处方）与发药记录，前者是收费的事实、后者是药事的事实，
     * 只有这条链路上的调用方（退费）能把两者对上。
     * <br>没有处方锚点的行（手工补记、挂号费等）一律放行 —— 它们本来就不对应任何实物。
     */
    void assertDrugReturnedForRefund(List<BizFeeRecord> rows, String scene);
}
