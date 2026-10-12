package com.his.pay.service;

import com.his.pay.vo.PayOrderVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付单服务（L3 收款凭证的唯一归属地）：扫码收款、渠道回调入账都只认这里，
 * 建单去重、金额与渠道的比对、状态翻转的原子性一并收在这层，别让每个调用方各写一遍闸。
 */
public interface PayOrderService {

    /**
     * 建单入参（内部指令，不是 HTTP 入参，所以字段校验由调用方负责）。
     *
     * @param bizType     业务类型（1-门诊缴费 2-挂号费 3-住院押金）
     * @param bizId       业务单ID（账单ID / 挂号单ID / 入院ID）
     * @param amount      本单应收金额，必须大于 0；金额必须由调用方按账单实时应缴算出来再传，不要传页面缓存值
     * @param channel     渠道（1-微信 2-支付宝 3-云闪付）
     */
    record CreateSpec(Integer bizType, Long bizId, Long patientId, String patientName,
                      BigDecimal amount, Integer channel, String remark) {
    }

    /**
     * 建待支付单。同一个业务单不会同时挂两张能付的码：
     * 已有同渠道待支付单且金额未变 → 复用原单号（渠道按商户订单号幂等返回同一张码）；
     * 金额变了或换了渠道 → 先关掉旧单再建新单，否则旧码还能被扫出来付款，而本地已不认它。
     */
    PayOrderVO createPending(CreateSpec spec);

    /**
     * 按支付单号取单；不存在返回 null（调用方决定是报错还是当「查无此单」）。
     */
    PayOrderVO getByPayNo(String payNo);

    /**
     * 渠道确认实收后的本地确认闸门：归属（单存在且渠道相符）+ 金额逐字相等 + 幂等翻转。
     *
     * <p>金额不符一律拒入账：渠道金额是付了多少钱的唯一事实，按本地金额入账等于替渠道改数字，
     * 收少了会给患者销多账，收多了就是一笔说不清的长款。
     *
     * @param channelAmount 渠道侧金额（通知里的 total_amount 或查单结果），null 视为不符
     * @return 翻转成功返回已支付的单（调用方据此推进记账）；返回 {@code null} 表示这张单早已入账（重复通知/并发），调用方直接跳过推进
     */
    PayOrderVO confirmPaid(String payNo, Integer channel, BigDecimal channelAmount, String channelTxnNo);

    /**
     * 关闭待支付单，并尽力关掉渠道侧交易（渠道失败只留痕，不回滚本地关闭）。
     * 已支付的单拒绝关闭 —— 钱收过了，要走退费而不是把凭证改成已关闭。
     */
    void closePending(String payNo);

    /**
     * 某患者最近的支付单（患者端「我的缴费记录」）。
     */
    List<PayOrderVO> listRecentByPatient(Long patientId, int limit);

    /**
     * 某业务单最近一条已支付的单；没有则返回 null（退费要先知道退的是哪一笔渠道收款）。
     */
    PayOrderVO latestPaidByBiz(Integer bizType, Long bizId);

    /**
     * 已支付 → 已退款（只翻状态并留退款原因；渠道侧原路退回由调用方负责）。
     * 已是已退款的单原样跳过，重复退号/退费通知不会把时间覆盖成新的一次。
     */
    void markRefunded(String payNo, String reason);
}
