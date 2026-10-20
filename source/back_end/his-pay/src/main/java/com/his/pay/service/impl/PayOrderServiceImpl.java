package com.his.pay.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.PayOrderStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.pay.entity.BizPayOrder;
import com.his.pay.mapper.BizPayOrderMapper;
import com.his.pay.service.AlipayChannelService;
import com.his.pay.service.PayOrderService;
import com.his.pay.support.PayQrCodeCache;
import com.his.pay.vo.PayOrderVO;
import com.his.system.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 支付单服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderServiceImpl extends ServiceImpl<BizPayOrderMapper, BizPayOrder> implements PayOrderService {

    private static final int AMOUNT_SCALE = 2;
    private static final int REMARK_MAX_LENGTH = 500;
    private static final int MAX_RECENT_ORDERS = 200;

    private final BizPayOrderMapper bizPayOrderMapper;

    private final RedisSequenceService redisSequenceService;

    private final AlipayChannelService alipayChannelService;

    private final PayQrCodeCache payQrCodeCache;

    /**
     * 「算不算同一张码」的唯一判据：建单复用与出码节流都走这里，不许在调用方各抄一份。
     */
    private static boolean isReusable(BizPayOrder valid, Integer channel, BigDecimal amount) {
        // 必须显式判「待支付」：按渠道取单之后，取回来的可能是这张已入账的凭证，
        // 把它当"可复用的码"返回给调用方就等于让界面重画一张早已付过的码
        return Objects.equals(valid.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())
                && !isExpired(valid)
                && Objects.equals(valid.getChannel(), channel)
                && valid.getAmount().compareTo(amount) == 0;
    }
    /**
     * 待支付单是否已过期。过期时刻由服务端现算（创建时间 + 有效期），不落列：
     * 库里存一份派生时间只会多出「时间到了列还没刷」的漂移窗口，创建时间才是唯一事实。
     */
    private static boolean isExpired(BizPayOrder order) {
        return order.getCreateTime() != null
                && !order.getCreateTime().plusMinutes(PayOrderService.PAY_ORDER_EXPIRE_MINUTES).isAfter(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateResult createPending(CreateSpec spec) {
        // 内部指令入参（跨模块调用，不经过参数绑定），Bean Validation 跑不到，只能在这里收口
        if (spec == null || spec.bizType() == null || spec.bizId() == null) {
            throw new BusinessException("支付单缺少业务归属（业务类型/业务单ID）");
        }
        // 渠道为 null 表示「聚合码」收款：一张码不绑定渠道，渠道在支付时由网关识别确定；
        // 非 null 必须是合法渠道（小程序等指定渠道的链路）
        if (spec.channel() != null && PayChannelEnum.payMethodOf(spec.channel()) == null) {
            throw new BusinessException("支付渠道不合法：" + PayChannelEnum.getLabelOrUnknown(spec.channel()));
        }
        if (spec.amount() == null) {
            throw new BusinessException("缺少支付金额");
        }
        BigDecimal amount = NumUtil.scale(spec.amount(), AMOUNT_SCALE);
        if (amount.signum() <= 0) {
            throw new BusinessException("支付金额必须大于 0");
        }

        BizPayOrder latest = selectLastValid(spec.bizType(), spec.bizId());
        if (latest != null && Objects.equals(latest.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
            throw new BusinessException("该业务单已收款（支付单 " + latest.getPayNo() + "），请勿重复收取");
        }
        // 只按「同一渠道形态」找可复用的码：聚合单（channel=null）只与聚合单比对，
        // 小程序等指定渠道的单只与同渠道比对 —— 两边互不抢复用，也不会把对方的码当成自己的。
        // 聚合码本身一张码多平台可扫，不存在「换渠道要重出」的问题，切档走的是同一张单。
        // 多张码并存的重复收款风险由付款侧兜住：钱从哪张进来，confirmPaid 后其余几张立刻作废。
        BizPayOrder sameChannel = selectLastValidOfChannel(spec.bizType(), spec.bizId(), spec.channel());
        if (sameChannel != null) {
            // 同渠道同金额且码未过期：复用原单号，渠道会幂等返回同一张码，不再产生第二张可付的码。
            // 过期不复用：旧码的倒计时已走完，复用等于让新码一出生就显示过期，必须关掉重出。
            if (isReusable(sameChannel, spec.channel(), amount)) {
                return new CreateResult(toVO(sameChannel), true);
            }
            // 同渠道但金额变了（窗口期间账被收过一部分）或原码已过期：这张旧码必须作废，
            // 否则同一个号能出两张码，患者扫哪张都可能付，而本地只认最后一张
            doClose(sameChannel);
        }

        BizPayOrder order = new BizPayOrder();
        order.setPayNo(redisSequenceService.generatePayOrderNo());
        order.setBizType(spec.bizType());
        order.setBizId(spec.bizId());
        order.setPatientId(spec.patientId());
        order.setPatientName(spec.patientName());
        order.setAmount(amount);
        order.setChannel(spec.channel());
        order.setPayStatus(PayOrderStatusEnum.PENDING.getCode());
        order.setRemark(spec.remark());
        bizPayOrderMapper.insert(order);
        log.info("[支付单] 新建 payNo={} 业务 {}#{} 渠道={} 金额={}", order.getPayNo(),
                spec.bizType(), spec.bizId(), PayChannelEnum.getLabel(spec.channel()), amount.toPlainString());
        return new CreateResult(toVO(order), false);
    }

    @Override
    public PayOrderVO reusablePending(Integer bizType, Long bizId, Integer channel, BigDecimal amount) {
        if (bizType == null || bizId == null) {
            throw new BusinessException("支付单缺少业务归属（业务类型/业务单ID）");
        }
        if (amount == null) {
            throw new BusinessException("缺少支付金额");
        }
        BizPayOrder valid = selectLastValidOfChannel(bizType, bizId, channel);
        if (valid == null || !isReusable(valid, channel, NumUtil.scale(amount, AMOUNT_SCALE))) {
            return null;
        }
        return toVO(valid);
    }

    /**
     * 该业务单当前挂着的有效单（待支付或已支付，取最新一条）。
     */
    private BizPayOrder selectLastValid(Integer bizType, Long bizId) {
        return bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, bizType)
                .eq(BizPayOrder::getBizId, bizId)
                .in(BizPayOrder::getPayStatus, PayOrderStatusEnum.PENDING.getCode(), PayOrderStatusEnum.PAID.getCode())
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1"));
    }

    /**
     * 该业务单上还活着的待支付单（取最新一条），没有返回 null。
     *
     * <p>只查待支付：已付/已关闭的单不是「一张还能扫的码」，拿它去复用就是把作废凭证重新摆到患者面前。
     *
     * <p><b>渠道为 null = 聚合码单</b>：聚合码不绑定渠道，复用只能按「本账单的聚合待支付单」匹配，
     * 所以用 {@code IS NULL}；指定渠道的链路（小程序）传具体渠道码走 {@code =} 分支。
     * 两种单靠 channel 列天然隔离，互不抢复用。
     */
    private BizPayOrder selectLastValidOfChannel(Integer bizType, Long bizId, Integer channel) {
        LambdaQueryWrapper<BizPayOrder> wrapper = new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, bizType)
                .eq(BizPayOrder::getBizId, bizId)
                .eq(BizPayOrder::getPayStatus, PayOrderStatusEnum.PENDING.getCode())
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1");
        if (channel == null) {
            wrapper.isNull(BizPayOrder::getChannel);
        } else {
            wrapper.eq(BizPayOrder::getChannel, channel);
        }
        return bizPayOrderMapper.selectOne(wrapper);
    }

    /**
     * 该业务单所有渠道上还活着的待支付单（切现金、关弹层时用）。
     */
    private List<BizPayOrder> selectPendingOfBiz(Integer bizType, Long bizId) {
        return bizPayOrderMapper.selectList(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, bizType)
                .eq(BizPayOrder::getBizId, bizId)
                .eq(BizPayOrder::getPayStatus, PayOrderStatusEnum.PENDING.getCode())
                .orderByDesc(BizPayOrder::getId));
    }

    @Override
    public PayOrderVO getByPayNo(String payNo) {
        BizPayOrder order = bizPayOrderMapper.selectByPayNo(payNo);
        return order == null ? null : toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderVO confirmPaid(String payNo, Integer channel, BigDecimal channelAmount, String channelTxnNo) {
        BizPayOrder order = bizPayOrderMapper.selectByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        // 聚合码单在出码时渠道为 null，支付时由网关决定实际渠道；
        // 只有「已明确登记过渠道」的单才做渠道一致性校验，避免把聚合单的首次入账拒掉
        if (order.getChannel() != null && !Objects.equals(order.getChannel(), channel)) {
            throw new BusinessException("支付单渠道与通知渠道不符，拒入账：" + payNo
                    + "（本单 " + PayChannelEnum.getLabelOrUnknown(order.getChannel()) + "）");
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
            // 已入过账：重复通知/并发到达都走这里，调用方据此跳过推进（幂等出口）
            return null;
        }
        // 金额逐字比对：渠道金额是「付了多少钱」的唯一事实，不符就入账等于替渠道改数字
        if (channelAmount == null || order.getAmount().compareTo(channelAmount) != 0) {
            throw new BusinessException("渠道金额与支付单不符，不入账：" + payNo
                    + "（本单 " + order.getAmount().toPlainString() + "，渠道 " + NumUtil.plain(channelAmount) + "）");
        }

        LocalDateTime payTime = LocalDateTime.now();
        if (bizPayOrderMapper.markPaidIfPending(payNo, channel, channelTxnNo, payTime) == 0) {
            // 条件更新没改成行 = 状态在这一次读之后被人改走了，重读按最新状态判，不猜
            BizPayOrder latest = bizPayOrderMapper.selectByPayNo(payNo);
            if (latest != null && Objects.equals(latest.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
                log.info("[支付单] 已由并发通知入账，本次跳过 payNo={}", payNo);
                return null;
            }
            throw new BusinessException("支付单状态不可推进（当前 "
                    + PayOrderStatusEnum.getLabelOrUnknown(latest == null ? null : latest.getPayStatus()) + "）：" + payNo);
        }
        order.setPayStatus(PayOrderStatusEnum.PAID.getCode());
        // 聚合码单出码时渠道为 null，这里把实际渠道落定（微信/支付宝/云闪付由网关识别后回填）
        order.setChannel(channel);
        order.setOutTradeNo(channelTxnNo);
        order.setPayTime(payTime);
        log.info("[支付单] 确认实收 payNo={} 渠道交易号={} 金额={}",
                payNo, channelTxnNo, order.getAmount().toPlainString());
        // 一张账单可能同时挂着聚合码与小程序侧指定渠道的码，钱从哪张进来，其余几张必须立刻作废：
        // 留着的每一张都是「同一笔账还能再付一次」的入口，而本地只会把多付的那笔记成长款
        closeOtherPendingOfBiz(order.getBizType(), order.getBizId(), payNo);
        return toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePending(String payNo) {
        BizPayOrder order = bizPayOrderMapper.selectByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        doClose(order);
    }

    @Override
    public List<PayOrderVO> listPendingByBiz(Integer bizType, Long bizId) {
        if (bizType == null || bizId == null) {
            throw new BusinessException("支付单缺少业务归属（业务类型/业务单ID）");
        }
        return selectPendingOfBiz(bizType, bizId).stream().map(this::toVO).toList();
    }

    @Override
    public void closePendingByBiz(Integer bizType, Long bizId) {
        if (bizType == null || bizId == null) {
            throw new BusinessException("支付单缺少业务归属（业务类型/业务单ID）");
        }
        // 有意不套事务：每张单的关闭本身是带状态条件的原子 UPDATE，一张失败不该把已关掉的那几张滚回去。
        // 「已收款」这种情况必须立刻抛给界面（见下），否则收银员收完现金才发现钱早从微信进来了
        for (BizPayOrder order : selectPendingOfBiz(bizType, bizId)) {
            try {
                doClose(order);
            } catch (BusinessException e) {
                // 钱已经进来的那张：不能悄悄跳过，让界面明确提示「该账已通过渠道完成」
                throw e;
            } catch (RuntimeException e) {
                // 其余失败（渠道关单超时之类）不阻断后续：本地单已置关闭，渠道侧那张由过期关单兜底
                log.warn("[支付单] 按业务单关闭待支付单失败 payNo={} err={}", order.getPayNo(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredIfPending(String payNo) {
        BizPayOrder order = bizPayOrderMapper.selectByPayNo(payNo);
        if (order == null || !Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())
                || !isExpired(order)) {
            return;
        }
        // 到期关单尽力而为：并发里患者刚付掉、别人刚关掉，条件更新都改不动行 → 一律按最新状态收场，
        // 不把「钱刚收进来」报成关单失败（本方法走轮询路径，调用方不该收到这个异常）
        if (bizPayOrderMapper.markClosedIfPending(payNo, LocalDateTime.now()) == 0) {
            return;
        }
        log.info("[支付单] 过期关单 payNo={}", payNo);
        payQrCodeCache.evict(payNo);
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            alipayChannelService.closeTrade(order.getPayNo());
        }
    }

    @Override
    public List<PayOrderVO> listRecentByPatient(Long patientId, int limit) {
        if (patientId == null) {
            throw new BusinessException("缺少患者");
        }
        return bizPayOrderMapper.selectList(new LambdaQueryWrapper<BizPayOrder>()
                        .eq(BizPayOrder::getPatientId, patientId)
                        .orderByDesc(BizPayOrder::getId)
                        .last("LIMIT " + Math.max(1, Math.min(limit, MAX_RECENT_ORDERS))))
                .stream().map(this::toVO).toList();
    }

    @Override
    public PayOrderVO latestPaidByBiz(Integer bizType, Long bizId) {
        BizPayOrder order = bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, bizType)
                .eq(BizPayOrder::getBizId, bizId)
                .eq(BizPayOrder::getPayStatus, PayOrderStatusEnum.PAID.getCode())
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1"));
        return order == null ? null : toVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRefunded(String payNo, String reason) {
        LocalDateTime now = LocalDateTime.now();
        if (bizPayOrderMapper.markRefundedIfPaid(payNo, TextUtil.cut(reason, REMARK_MAX_LENGTH), now) == 0) {
            // 改不动有两种：本来就已退款（幂等跳过），或压根不是已支付的单（不该发生，报出来让人查）
            PayOrderVO latest = getByPayNo(payNo);
            if (latest != null && Objects.equals(latest.getPayStatus(), PayOrderStatusEnum.REFUNDED.getCode())) {
                return;
            }
            throw new BusinessException("支付单不是已支付状态，无法登记退款：" + payNo
                    + "（当前 " + PayOrderStatusEnum.getLabelOrUnknown(latest == null ? null : latest.getPayStatus()) + "）");
        }
        log.info("[支付单] 登记退款 payNo={} 原因={}", payNo, reason);
    }

    /**
     * 作废同一业务单上其它渠道的待支付码（保留刚入账的那一张）。
     *
     * <p>失败只留 warn 绝不外抛：此刻钱已经在渠道侧收到了，本地唯一正确的动作是把这笔账记上；
     * 因为「另一张码关不掉」而回滚入账，等于把已经发生的收款从账上抹掉，比留一张待人工清理的活码严重得多。
     */
    private void closeOtherPendingOfBiz(Integer bizType, Long bizId, String keepPayNo) {
        for (BizPayOrder other : selectPendingOfBiz(bizType, bizId)) {
            if (Objects.equals(other.getPayNo(), keepPayNo)) {
                continue;
            }
            try {
                doClose(other);
                log.info("[支付单] 收款后作废同账单其它渠道的码 payNo={} channel={}",
                        other.getPayNo(), PayChannelEnum.getLabelOrUnknown(other.getChannel()));
            } catch (RuntimeException e) {
                log.warn("[支付单] 收款后关闭其它渠道待支付单失败 payNo={} keep={} err={}",
                        other.getPayNo(), keepPayNo, e.getMessage());
            }
        }
    }

    private void doClose(BizPayOrder order) {
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.CLOSED.getCode())) {
            return;
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
            // 钱已经收进来了，关闭等于把收款凭证抹掉，日结时会变成一笔找不到出处的长款
            throw new BusinessException("支付单已收款，不能关闭，请走退费：" + order.getPayNo());
        }
        if (bizPayOrderMapper.markClosedIfPending(order.getPayNo(), LocalDateTime.now()) == 0) {
            BizPayOrder latest = bizPayOrderMapper.selectByPayNo(order.getPayNo());
            if (latest != null && Objects.equals(latest.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
                throw new BusinessException("支付单刚刚已收款，不能关闭，请走退费：" + order.getPayNo());
            }
            return;
        }
        log.info("[支付单] 关闭 payNo={}", order.getPayNo());
        // 码串缓存跟着单一起作废：留着它，下次出码会读到「这张码还在」而重画一张渠道侧早已关掉的码，
        // 患者扫出来只会看到「交易已关闭」，收银员却以为码是新出的
        payQrCodeCache.evict(order.getPayNo());
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            alipayChannelService.closeTrade(order.getPayNo());
        }
    }

    private PayOrderVO toVO(BizPayOrder order) {
        PayOrderVO vo = new PayOrderVO();
        vo.setId(order.getId());
        vo.setPayNo(order.getPayNo());
        vo.setBizType(order.getBizType());
        vo.setBizId(order.getBizId());
        vo.setPatientId(order.getPatientId());
        vo.setPatientName(order.getPatientName());
        vo.setAmount(order.getAmount());
        vo.setChannel(order.getChannel());
        vo.setPayStatus(order.getPayStatus());
        vo.setOutTradeNo(order.getOutTradeNo());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getCreateTime() == null ? null
                : order.getCreateTime().plusMinutes(PayOrderService.PAY_ORDER_EXPIRE_MINUTES));
        vo.setCashierId(order.getCreateById());
        vo.setCashierName(order.getCreateBy());
        return vo;
    }
}
