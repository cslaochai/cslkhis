package com.his.api.service;

import com.his.api.vo.PayOrderVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付单服务（L3 收款凭证的唯一归属地）：扫码收款、渠道回调入账都只认这里，
 * 建单去重、金额与渠道的比对、状态翻转的原子性一并收在这层，别让每个调用方各写一遍闸。
 */
public interface PayOrderProvider {

    /**
     * 支付码有效期（分钟）：待支付单自创建起超过该时长即视为过期。
     * 出码倒计时（expireTime）、查单侧到期关单、建单复用判断共用这一个口径；
     * 渠道侧的 timeout_express 也取同一值，两侧同刻失效。
     */
    int PAY_ORDER_EXPIRE_MINUTES = 5;

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
     * 建单结果：{@code reused} 表示命中原有的可复用待支付单（没有新建、也没关任何单）。
     *
     * <p>调用方必须按这个标志分支：复用意味着<b>渠道侧那笔预下单还在、码串没变</b>，
     * 再打一次出码接口只是把同一句话问渠道要一遍，白给渠道加量。
     */
    record CreateResult(PayOrderVO order, boolean reused) {
    }

    /**
     * 建待支付单。同一个业务单不会同时挂两张能付的码：
     * 已有同渠道、金额未变、且未过期的待支付单 → 复用原单号（渠道按商户订单号幂等返回同一张码）；
     * 金额变了、换了渠道或原码已过期 → 先关掉旧单再建新单，否则旧码还能被扫出来付款，而本地已不认它。
     */
    CreateResult createPending(CreateSpec spec);

    /**
     * 当前<b>可复用</b>的待支付单：同渠道、同金额、未过期，没有则返回 null。
     *
     * <p>判据与 {@link #createPending} 内部完全同一个（共用一个实现），不要在调用方再抄一份 ——
     * 两份「算不算同一张码」的口径迟早漂移成「一处复用、一处重出」。
     *
     * <p>为什么要单独暴露：出码侧的节流闸要在<b>决定建新单之前</b>就知道「这次能不能只回原码」。
     * 直接调 {@link #createPending} 会在换渠道/金额变化时顺手关掉患者手上那张还没扫完的码，
     * 那种干扰比多一次请求贵得多，所以间隔内的请求必须先问一句再决定放不放行。
     */
    PayOrderVO reusablePending(Integer bizType, Long bizId, Integer channel, BigDecimal amount);

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
     * 已到期的待支付单做收尾关单（含渠道关单）：到期未付的码不该继续可扫。
     * 与 closePending 的差别是「尽力而为」——并发里患者刚付掉就什么都不做，
     * 不把「钱已进来」报成关单失败；供查单轮询路径定时调用（未到期时是空操作）。
     */
    void closeExpiredIfPending(String payNo);

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
