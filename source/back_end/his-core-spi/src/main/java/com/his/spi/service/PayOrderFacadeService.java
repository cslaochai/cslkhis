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
}
