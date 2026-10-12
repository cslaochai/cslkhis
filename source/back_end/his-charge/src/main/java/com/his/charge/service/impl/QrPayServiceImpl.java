package com.his.charge.service.impl;

import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.service.PaymentService;
import com.his.charge.service.QrPayService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.utils.PayQrCodeUtils;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.PayChannelEnum;
import com.his.common.enums.PayOrderStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.enums.TxnSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.pay.enums.PayOrderBizTypeEnum;
import com.his.pay.service.AlipayChannelService;
import com.his.pay.service.PayOrderService;
import com.his.pay.service.WxPayChannelService;
import com.his.pay.vo.PayOrderVO;
import com.his.system.support.SystemOperatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 收费台扫码收款实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QrPayServiceImpl implements QrPayService {

    /**
     * 渠道协议应答原文（不是本库码值）：支付宝只看 body 是否等于 success，其余一律按失败重试。
     */
    private static final String NOTIFY_ACK = "success";
    private static final String NOTIFY_RETRY = "failure";

    private static final int QR_SIZE = 256;
    private static final int AMOUNT_SCALE = 2;

    private final SettlementBillService settlementBillService;

    private final PaymentService paymentService;

    private final PayOrderService payOrderService;

    private final AlipayChannelService alipayChannelService;

    private final WxPayChannelService wxPayChannelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrPayCreateVO create(QrPayCreateDTO dto) {
        BizSettlementBill bill = settlementBillService.getById(dto.getBillId());
        if (bill == null) {
            throw new BusinessException("结算账单不存在：" + dto.getBillId());
        }
        if (Objects.equals(bill.getBillStatus(), BillStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("账单已作废，不能收款：" + bill.getBillNo());
        }
        BigDecimal unpaid = unpaidOf(bill);
        if (unpaid.signum() <= 0) {
            throw new BusinessException("该账单已无应缴金额");
        }

        PayOrderVO order = payOrderService.createPending(new PayOrderService.CreateSpec(
                PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode(), bill.getId(),
                bill.getPatientId(), bill.getPatientName(), unpaid, dto.getChannel(),
                "收费台扫码收款 账单 " + bill.getBillNo()));

        WxPayChannelService.PayUnifiedResult unified = precreateOfChannel(order);
        if (unified.errMsg() != null) {
            throw new BusinessException(unified.errMsg());
        }
        String qrImage = PayQrCodeUtils.generateDataUri(unified.payParams().get("qrCode"), QR_SIZE);
        if (!TextUtil.hasText(qrImage)) {
            throw new BusinessException("二维码生成失败，请重试");
        }

        QrPayCreateVO vo = new QrPayCreateVO();
        vo.setPayNo(order.getPayNo());
        vo.setChannel(order.getChannel());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setQrImage(qrImage);
        vo.setExpireTime(order.getExpireTime());
        vo.setServerTime(LocalDateTime.now());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrPayResultVO queryResult(String payNo) {
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            settleFromChannel(order);
            order = payOrderService.getByPayNo(payNo);
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            // 过期未付由服务端关单（含渠道关单），前端据此渲染「已过期 + 刷新」；
            // 并发付款的钱优先，关不动就按最新状态返回
            payOrderService.closeExpiredIfPending(payNo);
            order = payOrderService.getByPayNo(payNo);
        }
        return toResultVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(String payNo) {
        payOrderService.closePending(payNo);
    }

    @Override
    public void mockMarkPaid(String payNo) {
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            throw new BusinessException("仅支付宝出码支持模拟收款，当前渠道："
                    + PayChannelEnum.labelOrUnknown(order.getChannel()));
        }
        // 金额取支付单自己的应缴额：模拟的是「患者照单全付」，要模拟少付就得改传参，
        // 而那条路径该由真渠道或退费流程覆盖，不在这里开口子
        alipayChannelService.markMockPaid(payNo, order.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmByAlipayNotify(Map<String, String> params) {
        String payNo = params == null ? null : params.get("out_trade_no");

        // 闸1 身份：签名由支付宝私钥产出，改一个字节签名就断；再认 app_id，别人的应用签得再对也不是本应用的收款
        if (!alipayChannelService.verifyNotify(params)) {
            log.warn("[支付宝回调·收费侧] 验签未通过，不入账 payNo={}", payNo);
            return NOTIFY_RETRY;
        }
        // 「未付款」的通知（用户取消、交易超时关闭）是合法通知：确认收到即可，
        // 回 failure 会让支付宝对一条永远不会变成已付款的通知重试整天
        if (!alipayChannelService.isPaidStatus(params.get("trade_status"))) {
            log.info("[支付宝回调·收费侧] 交易未处于已付款状态 payNo={} tradeStatus={}", payNo, params.get("trade_status"));
            return NOTIFY_ACK;
        }
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            log.error("[支付宝回调·收费侧] 通知里的支付单不存在，疑似跨系统串单 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            return NOTIFY_RETRY;
        }

        // 回调这一侧没有登录态，而收款流水必须写清是谁收的：唯一可归因的事实是「这张码是谁开出来的」，
        // 出码人在支付单的创建人字段上（取自我方库，不是通知报文里的字段）
        SystemOperatorContext.bindEmployee(order.getCashierId(), TextUtil.trimToEmpty(order.getCashierName()));
        try {
            // 闸2+3+4（归属/金额/幂等翻转）在支付单服务里，与患者端回调共用同一套口径
            PayOrderVO paid = payOrderService.confirmPaid(payNo, PayChannelEnum.ALIPAY.getCode(),
                    parseAmount(params.get("total_amount")), params.get("trade_no"));
            if (paid == null) {
                log.info("[支付宝回调·收费侧] 支付单已入账，重复通知直接应答 payNo={}", payNo);
                return NOTIFY_ACK;
            }
            bookBill(paid);
            log.info("[支付宝回调·收费侧] 已入账 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            return NOTIFY_ACK;
        } finally {
            SystemOperatorContext.clear();
        }
    }

    @Override
    public String confirmByWxNotify(String body) {
        log.error("[微信回调·收费侧] 微信通知通道未接通（Native 出码与 V3 验签解密均未实现），本次通知不受理");
        return NOTIFY_RETRY;
    }

    /**
     * 渠道查单兜底入账：公网回调断了也要把患者已付的钱记上账。
     */
    private void settleFromChannel(PayOrderVO order) {
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            throw new BusinessException("该渠道暂不支持主动查单，请等待渠道回调："
                    + PayChannelEnum.labelOrUnknown(order.getChannel()));
        }
        // 查单是网络调用，留在本事务里是为了让「翻转 + 记账」保持原子：
        // 拆成两段就会出现「支付单已置已支付、账单却没记收款」的永久漏账
        AlipayChannelService.TradeState state = alipayChannelService.queryTrade(order.getPayNo());
        if (state == null || !alipayChannelService.isPaidStatus(state.tradeStatus())) {
            return;
        }
        PayOrderVO paid = payOrderService.confirmPaid(order.getPayNo(), PayChannelEnum.ALIPAY.getCode(),
                state.totalAmount(), state.tradeNo());
        if (paid != null) {
            bookBill(paid);
        }
        log.info("[扫码收款] 查单兜底入账 payNo={} tradeNo={}", order.getPayNo(), state.tradeNo());
    }

    /**
     * 到账入账：给账单记一笔渠道收款流水（与柜面现金同一个入口，只是来源写「收费台收款」、带上渠道交易号）。
     *
     * <p>入账金额取<b>渠道实收与账单仍欠的较小者</b>：码开出来之后窗口可能又收过一笔，
     * 按支付单金额收就是多收；而患者实际付过的钱必须原样记上，多付的那部分留给日结勾对，不能销在别的账上。
     */
    private void bookBill(PayOrderVO order) {
        if (!Objects.equals(order.getBizType(), PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode())) {
            throw new BusinessException("该支付单不是账单收款单，无法入账：" + order.getPayNo());
        }
        BizSettlementBill bill = settlementBillService.getById(order.getBizId());
        if (bill == null) {
            throw new BusinessException("账单收款推进失败：结算账单不存在（" + order.getBizId() + "）");
        }
        PaymentMethodEnum payMethod = PayChannelEnum.payMethodOf(order.getChannel());
        if (payMethod == null) {
            // 兜成现金等于把渠道收的钱塞进抽屉，当天清点必然多出一笔说不清的差额
            throw new BusinessException("渠道无对应收款方式，不入账："
                    + PayChannelEnum.labelOrUnknown(order.getChannel()));
        }
        BigDecimal amount = unpaidOf(bill).min(order.getAmount());
        if (amount.signum() <= 0) {
            log.error("[扫码收款] 账单 {} 已无应缴但渠道实收 ￥{}（支付单 {}），钱已进渠道、本地未销账，请走退费或日结勾对",
                    bill.getId(), order.getAmount().toPlainString(), order.getPayNo());
            return;
        }

        BillPayDTO dto = new BillPayDTO();
        dto.setBillId(bill.getId());
        dto.setSourceType(TxnSourceEnum.CASHIER.getCode());
        BillPayDTO.PayItem item = new BillPayDTO.PayItem();
        item.setPayMethod(payMethod.getCode());
        item.setAmount(amount);
        item.setChannelTxnNo(order.getOutTradeNo());
        item.setRemark("收费台扫码收款 支付单 " + order.getPayNo());
        dto.setItems(List.of(item));
        paymentService.pay(dto);
        log.info("[扫码收款] 已入账 payNo={} billId={} amount=￥{}",
                order.getPayNo(), bill.getId(), amount.toPlainString());
    }

    private WxPayChannelService.PayUnifiedResult precreateOfChannel(PayOrderVO order) {
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            return alipayChannelService.precreate(order);
        }
        if (Objects.equals(order.getChannel(), PayChannelEnum.WECHAT.getCode())) {
            return wxPayChannelService.nativePrecreate(order);
        }
        throw new BusinessException("该渠道暂不支持出码收款：" + PayChannelEnum.labelOrUnknown(order.getChannel()));
    }

    private BigDecimal unpaidOf(BizSettlementBill bill) {
        return NumUtil.scale(NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount())),
                AMOUNT_SCALE);
    }

    private BigDecimal parseAmount(String text) {
        if (!TextUtil.hasText(text)) {
            return null;
        }
        try {
            return new BigDecimal(TextUtil.trim(text)).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.error("[支付宝回调·收费侧] total_amount 不是合法金额 raw={}", text);
            return null;
        }
    }

    private QrPayResultVO toResultVO(PayOrderVO order) {
        QrPayResultVO vo = new QrPayResultVO();
        vo.setPayNo(order.getPayNo());
        vo.setPayStatus(order.getPayStatus());
        vo.setAmount(order.getAmount());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setServerTime(LocalDateTime.now());
        vo.setBillId(order.getBizId());
        BizSettlementBill bill = settlementBillService.getById(order.getBizId());
        vo.setUnpaidAmount(bill == null ? null : unpaidOf(bill));
        return vo;
    }
}
