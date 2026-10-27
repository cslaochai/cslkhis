package com.his.charge.service;

/**
 * 门诊医保实时结算（收款窗口调 2201 预结算 / 2301 正式结算）。
 *
 * <p>与 {@link InsuranceSettlementService} 的区别：那个是<b>收完钱之后</b>给医保局报盘的清单管理；
 * 这个是<b>收款那一刻</b>跟医保平台实时交互的入口 —— 刷社保卡、算 splits、扣个账。
 */
public interface InsuranceRealTimeSettleService {

    /**
     * 降级自费账单的标记（写入账单 remark）。医保补结算只认带此标记的已支付自费单，
     * 防止对普通自费单误操作。
     */
    String DEGRADE_MARKER = "[医保降级]";

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
     *
     * <p>失败语义（防双收）：确定失败（请求未达/医保明确拒绝）直接抛业务异常，调用方可降自费；
     * <b>不确定失败（超时/连接中断）先自动发 2305 冲正确认</b>——医保侧有单则冲正、无单则确认，
     * 冲正通道也不通时抛「医保侧状态未知」异常，调用方禁止降自费。
     */
    InsuranceChannelService.SettleSplit formalSettle(Long billId);

    /**
     * 医保补结算预检（不落库）：校验账单能否补结算 + 2201 试算拿 splits 预览。
     * 返回的 split.infcode=0 表示可补，errMsg 带不可补原因。
     */
    InsuranceChannelService.SettleSplit supplementCheck(Long billId);

    /**
     * 医保补结算：对「医保实时结算失败被降级自费、患者已全额付清」的账单，医保恢复后补走医保报销。
     * 同事务：
     * <ol>
     *   <li>参保核验（停保/未参保拒绝）</li>
     *   <li>2201 试算 → 2301 正式结算（tradeNo = 账单号-2301S，扣个账出 YBZ 单号）</li>
     *   <li>落个账收款流水 + 回写账单 splits（复用 2301 回写逻辑）</li>
     *   <li>原路退回患者多付的现金差额（统筹 + 个账部分，方向=退，来源=收费处直退）</li>
     *   <li>账单 settlementMode 回写 2-医保，票据按「钱票同真」口径同步作废待重开</li>
     * </ol>
     * 2301 之后的本地步骤若失败，同事务回滚并自动对医保侧发 2305 冲正，不留两边不一致。
     */
    InsuranceChannelService.SettleSplit supplement(Long billId, String reason);
}
