package com.his.spi.service;

import com.his.spi.dto.PayOrderCreateDTO;
import com.his.spi.vo.PayOrderCreateResultVO;
import com.his.spi.vo.PayOrderInfoVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付单端口：收款凭证这一层的建单、取单、实收确认与关单，去重与状态翻转的原子性收在实现方。
 *
 * <p>事务口径：方法加入调用方事务，调用方负责整条「翻转 + 入账」的原子性。
 */
public interface PayOrderFacadeService {

    /**
     * 建待支付单：同渠道、金额未变且未过期的码直接复用原单号，金额变了或原码过期则先关旧单再建新单。
     * 用于收费台扫码收款出码。
     *
     * @return 支付单与「是否复用」标志；复用意味着渠道侧那张码还在，调用方不得再打一次渠道出码
     */
    PayOrderCreateResultVO createPending(PayOrderCreateDTO dto);

    /**
     * 当前可复用的待支付单（同渠道、同金额、未过期），没有返回 null。
     * 用于出码节流闸之前先问一句「这次能不能只回原码」，判据与建单同一个实现，调用方不许再抄一份。
     */
    PayOrderInfoVO reusablePending(Integer bizType, Long bizId, Integer channel, BigDecimal amount);

    /**
     * 按支付单号取单；不存在返回 null，由调用方决定报错还是当查无此单。用于收款结果轮询与回调入账。
     */
    PayOrderInfoVO getByPayNo(String payNo);

    /**
     * 渠道确认实收后的本地确认闸门：归属核对 + 金额逐字比对 + 幂等翻转。
     * 用于支付宝回调与查单兜底两条入账链路，两侧共用同一套口径。
     *
     * @param channelAmount 渠道侧金额，null 视为不符
     * @return 翻转成功返回已支付的单（调用方据此推进记账）；返回 null 表示这张单早已入账（重复通知或并发），调用方跳过推进
     */
    PayOrderInfoVO confirmPaid(String payNo, Integer channel, BigDecimal channelAmount, String channelTxnNo);

    /**
     * 关闭待支付单并尽力关掉渠道侧交易。已支付的单拒绝关闭——钱收过了要走退费，不能把凭证改成已关闭。
     * 用于收银台关掉某一张码。
     */
    void closePending(String payNo);

    /**
     * 关闭某业务单下所有渠道的待支付单。用于收银员切到现金或关掉收款弹层——
     * 只关界面上那一张，剩下的那张还是能被扫出来付，而现金已经进抽屉。
     *
     * <p>遇到「刚被患者付掉」的单抛业务异常，让界面明确提示该账已完成。
     */
    void closePendingByBiz(Integer bizType, Long bizId);

    /**
     * 某业务单当前还活着的待支付单（0 张、1 张或多张）。
     * 用于关单前先知道该账单实际挂过哪几个渠道，好逐一释放节流窗口与码串缓存。
     */
    List<PayOrderInfoVO> listPendingByBiz(Integer bizType, Long bizId);

    /**
     * 已到期的待支付单做收尾关单（含渠道关单）。用于轮询查单侧的到期兜底，未到期时是空操作。
     *
     * <p>尽力而为：并发里患者刚付掉就什么都不做，不把「钱已进来」报成关单失败。
     */
    void closeExpiredIfPending(String payNo);

    /**
     * 患者端「我的支付单」：按患者ID取最近 {@code limit} 条支付单（按建单先后倒序），
     * 供患者端缴费记录列表展示（单号/业务类型/业务单/金额/支付状态/支付时间）。
     */
    List<PayOrderInfoVO> listRecentByPatient(Long patientId, int limit);

    /**
     * 按业务单取最近一条已支付单：下单前幂等查重（已收过款的业务单不再建第二张单）、
     * 退号/退费时定位要退的原支付单。没有已支付单返回 null，由调用方决定报错还是走新建。
     */
    PayOrderInfoVO findLatestPaid(Integer bizType, Long bizId);

    /**
     * 已支付单置已退款：只翻状态并留退款原因（渠道原路退回由调用方负责）。
     * 已是已退款的单原样跳过，重复退号/退费通知不会把退款时间覆盖成新的一次；
     * 不是已支付状态的单拒绝登记，报出来让人查。
     */
    void markRefunded(String payNo, String reason);
}
