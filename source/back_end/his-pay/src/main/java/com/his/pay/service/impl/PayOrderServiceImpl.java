package com.his.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.PayOrderStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.pay.entity.BizPayOrder;
import com.his.pay.mapper.BizPayOrderMapper;
import com.his.pay.service.AlipayChannelService;
import com.his.pay.service.PayOrderService;
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
public class PayOrderServiceImpl implements PayOrderService {

    private static final int AMOUNT_SCALE = 2;
    private static final int REMARK_MAX_LENGTH = 500;
    private static final int MAX_RECENT_ORDERS = 200;

    private final BizPayOrderMapper bizPayOrderMapper;

    private final RedisSequenceService redisSequenceService;

    private final AlipayChannelService alipayChannelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderVO createPending(CreateSpec spec) {
        // 内部指令入参（跨模块调用，不经过参数绑定），Bean Validation 跑不到，只能在这里收口
        if (spec == null || spec.bizType() == null || spec.bizId() == null) {
            throw new BusinessException("支付单缺少业务归属（业务类型/业务单ID）");
        }
        if (PayChannelEnum.payMethodOf(spec.channel()) == null) {
            throw new BusinessException("支付渠道不合法：" + PayChannelEnum.labelOrUnknown(spec.channel()));
        }
        if (spec.amount() == null) {
            throw new BusinessException("缺少支付金额");
        }
        BigDecimal amount = NumUtil.scale(spec.amount(), AMOUNT_SCALE);
        if (amount.signum() <= 0) {
            throw new BusinessException("支付金额必须大于 0");
        }

        BizPayOrder valid = bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, spec.bizType())
                .eq(BizPayOrder::getBizId, spec.bizId())
                .in(BizPayOrder::getPayStatus, PayOrderStatusEnum.PENDING.getCode(), PayOrderStatusEnum.PAID.getCode())
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1"));
        if (valid != null) {
            if (Objects.equals(valid.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
                throw new BusinessException("该业务单已收款（支付单 " + valid.getPayNo() + "），请勿重复收取");
            }
            // 同渠道同金额：复用原单号，渠道会幂等返回同一张码，不再产生第二张可付的码
            if (Objects.equals(valid.getChannel(), spec.channel()) && valid.getAmount().compareTo(amount) == 0) {
                return toVO(valid);
            }
            // 换了渠道或金额变了（窗口期间账被收过一部分）：旧码必须作废再出新码，
            // 否则两张码同时有效，患者扫哪张都可能付，而本地只认最后一张
            doClose(valid);
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
                spec.bizType(), spec.bizId(), PayChannelEnum.getText(spec.channel()), amount.toPlainString());
        return toVO(order);
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
        if (!Objects.equals(order.getChannel(), channel)) {
            throw new BusinessException("支付单渠道与通知渠道不符，拒入账：" + payNo
                    + "（本单 " + PayChannelEnum.labelOrUnknown(order.getChannel()) + "）");
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
        if (bizPayOrderMapper.markPaidIfPending(payNo, channelTxnNo, payTime) == 0) {
            // 条件更新没改成行 = 状态在这一次读之后被人改走了，重读按最新状态判，不猜
            BizPayOrder latest = bizPayOrderMapper.selectByPayNo(payNo);
            if (latest != null && Objects.equals(latest.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
                log.info("[支付单] 已由并发通知入账，本次跳过 payNo={}", payNo);
                return null;
            }
            throw new BusinessException("支付单状态不可推进（当前 "
                    + PayOrderStatusEnum.labelOrUnknown(latest == null ? null : latest.getPayStatus()) + "）：" + payNo);
        }
        order.setPayStatus(PayOrderStatusEnum.PAID.getCode());
        order.setOutTradeNo(channelTxnNo);
        order.setPayTime(payTime);
        log.info("[支付单] 确认实收 payNo={} 渠道交易号={} 金额={}",
                payNo, channelTxnNo, order.getAmount().toPlainString());
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
                    + "（当前 " + PayOrderStatusEnum.labelOrUnknown(latest == null ? null : latest.getPayStatus()) + "）");
        }
        log.info("[支付单] 登记退款 payNo={} 原因={}", payNo, reason);
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
        vo.setCashierId(order.getCreateById());
        vo.setCashierName(order.getCreateBy());
        return vo;
    }
}
