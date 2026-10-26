package com.his.charge.service;

/**
 * 门诊医保实时结算（收款窗口调 2201 预结算 / 2301 正式结算）。
 *
 * <p>与 {@link InsuranceSettlementService} 的区别：那个是<b>收完钱之后</b>给医保局报盘的清单管理；
 * 这个是<b>收款那一刻</b>跟医保平台实时交互的入口 —— 刷社保卡、算 splits、扣个账。
 */
public interface InsuranceRealTimeSettleService {

    /**
     * 2201 预结算：按账单组费用明细推给医保平台，返回统筹/个账/自付 splits，不落库。
     * 用于收费员点「医保个账」后、患者确认前展示 splits。
     */
    InsuranceChannelService.SettleSplit preSettle(Long billId);

    /**
     * 2301 正式结算：医保平台当场扣个账、出 YBZ 单号。
     * 成功后同事务：
     * <ol>
     *   <li>落 biz_payment_txn（pay_method=4 医保个账，direction=1）</li>
     *   <li>回写账单头 pool_amount / account_amount / self_amount / payable_amount</li>
     *   <li>按行金额占比把 splits 摊到账单行（2304 报盘要逐行费率）</li>
     *   <li>调 {@code SettlementBillService.refreshFromTxns} 刷新账单 paid_amount / 状态</li>
     * </ol>
     */
    InsuranceChannelService.SettleSplit formalSettle(Long billId);
}
