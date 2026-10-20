package com.his.charge.service.impl;

import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.service.PaymentService;
import com.his.charge.service.QrPayService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.PayQrCodeUtils;
import com.his.common.util.TextUtil;
import com.his.pay.service.*;
import com.his.pay.support.AggregatedQrSupport;
import com.his.pay.support.PayQrCodeCache;
import com.his.pay.vo.PayOrderVO;
import com.his.system.support.SystemOperatorContext;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 收费台扫码收款实现（聚合支付码：医院自有聚合网关，一张码微信/支付宝/云闪付都能扫）。
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

    /**
     * 同一张账单两次「必须重出码」之间的最小间隔（秒）。
     * 聚合码本身只是把支付单号拼进一个网关 URL，没有渠道网络开销，所以这道闸只防「连点/脚本刷」，
     * 不挡业务：缓存里已经有码的请求走不到这里（见 create 的第 1 道），切现金、点刷新、重开弹层都不会被它拦。
     */
    private static final int QR_ISSUE_INTERVAL_SECONDS = 20;

    /**
     * 单个收银员在一个统计窗口内允许的出码次数上限（防的是「换着账单批量刷」）。
     */
    private static final int QR_ISSUE_QUOTA_PER_WINDOW = 30;

    /**
     * 出码配额的统计窗口（秒）。固定窗口、自第一次出码起算，不滑动。
     */
    private static final int QR_ISSUE_QUOTA_WINDOW_SECONDS = 60;

    /**
     * 同一张支付单两次「真的去查单」之间的最小间隔（秒）。到账主要靠渠道通知，查单只是兜底。
     */
    private static final int CHANNEL_QUERY_INTERVAL_SECONDS = 5;

    /**
     * 出码间隔闸（key = 前缀 + 账单ID）、收银员配额闸、查单节流闸，
     * Redis 口径与 fail-open 策略统一收口在 {@link PayRateLimitService}，本类只定阈值与 key。
     */
    private static final String ISSUE_GATE_KEY_PREFIX = "his:pay:qr:issue:";
    private static final String ISSUE_QUOTA_KEY_PREFIX = "his:pay:qr:quota:";
    private static final String CHANNEL_QUERY_KEY_PREFIX = "his:pay:qr:query:";

    private final SettlementBillService settlementBillService;

    private final PaymentService paymentService;

    private final PayOrderService payOrderService;

    private final AlipayChannelService alipayChannelService;

    private final WxPayChannelService wxPayChannelService;

    private final PayNotifyLogService payNotifyLogService;

    private final PayRateLimitService payRateLimitService;

    /**
     * 码串缓存（按支付单号，聚合码一张单一个号，天然一对一）
     */
    private final PayQrCodeCache payQrCodeCache;

    /**
     * 聚合支付码支撑：拼网关 URL、模拟模式下的收款登记与查单
     */
    private final AggregatedQrSupport aggregatedQrSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrPayCreateVO create(QrPayCreateDTO qrPayCreateDTO) {
        BizSettlementBill bill = settlementBillService.getById(qrPayCreateDTO.getBillId());
        if (bill == null) {
            throw new BusinessException("结算账单不存在：" + qrPayCreateDTO.getBillId());
        }
        if (Objects.equals(bill.getBillStatus(), BillStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("账单已作废，不能收款：" + bill.getBillNo());
        }
        BigDecimal unpaid = unpaidOf(bill);
        if (unpaid.signum() <= 0) {
            throw new BusinessException("该账单已无应缴金额");
        }

        // 第 1 道：这张账单是不是已经有一张活着的聚合码？有就直接本地重画。
        // 位置必须在节流闸之前 —— 收银员点刷新、重开收款弹层、从现金切回扫码，最终都走这里；
        // 任何一次被闸拦下，都是把「码就在手里」说成「你点太快了」（现场看到的就是这句）。
        // 聚合码的渠道为 null，复用按「本账单的聚合待支付单」匹配（见 PayOrderServiceImpl）
        PayOrderVO reusable = payOrderService.reusablePending(
                PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode(), bill.getId(), null, unpaid);
        if (reusable != null) {
            String cachedQrCode = payQrCodeCache.get(reusable.getPayNo());
            if (TextUtil.hasText(cachedQrCode)) {
                return toCreateVO(bill, reusable, cachedQrCode);
            }
        }

        // 第 2 道 间隔闸：只有真要去生成码才需要理由。key 按账单（一张账单只有一张聚合码），
        // 点刷新、重开弹层、现金切回扫码都走第 1 道缓存，走不到这里
        String gateKey = issueGateKey(bill.getId());
        Long retryAfter = payRateLimitService.intervalRetryAfter(gateKey, QR_ISSUE_INTERVAL_SECONDS);
        if (retryAfter != null) {
            throw new BusinessException("收款码正在重新生成，请 " + retryAfter + " 秒后再试");
        }

        // 第 3 道 人员配额：按收银员封顶次数，账单维度的间隔闸管不到「换个账单继续刷」
        payRateLimitService.assertQuota(ISSUE_QUOTA_KEY_PREFIX + UserUtils.getCurrentUser().getEmployeeId(),
                QR_ISSUE_QUOTA_PER_WINDOW, QR_ISSUE_QUOTA_WINDOW_SECONDS, "出码");

        // 本次没真把码出来就得把间隔闸的窗口退回去：建单、出码、画图任一步失败都不能让收银员干等 20 秒
        try {
            // 聚合码：渠道为 null，渠道在支付时由网关识别确定
            PayOrderService.CreateResult created = payOrderService.createPending(new PayOrderService.CreateSpec(
                    PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode(), bill.getId(),
                    bill.getPatientId(), bill.getPatientName(), unpaid, null,
                    "收费台扫码收款（聚合码）账单 " + bill.getBillNo()));
            PayOrderVO order = created.order();

            // 复用命中的单：码串在缓存里，取回本地画图，不再生成
            String qrCode = created.reused() ? payQrCodeCache.get(order.getPayNo()) : null;
            if (!TextUtil.hasText(qrCode)) {
                // 聚合码 = 医院聚合收款页地址 + 支付单号，不进任何渠道预下单
                qrCode = aggregatedQrSupport.buildQrContent(order.getPayNo());
                payQrCodeCache.put(order.getPayNo(), qrCode);
            }

            return toCreateVO(bill, order, qrCode);
        } catch (RuntimeException e) {
            payRateLimitService.release(gateKey);
            throw e;
        }
    }

    /**
     * 关掉这张账单上所有待支付码（收银员切到现金、关掉收款弹层时调）。
     *
     * <p>一并释放出码闸与码串缓存：单都关了，那张码就不存在了，留着闸只会让「收不了现金、切回扫码」
     * 的收银员再干等 20 秒，留着缓存则会重画一张已关掉的码。
     */
    @Override
    public void closePendingByBill(Long billId) {
        if (billId == null) {
            throw new BusinessException("缺少账单");
        }
        int bizType = PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode();
        for (PayOrderVO pending : payOrderService.listPendingByBiz(bizType, billId)) {
            payRateLimitService.release(issueGateKey(billId));
            payQrCodeCache.evict(pending.getPayNo());
        }
        payOrderService.closePendingByBiz(bizType, billId);
    }

    /**
     * 出码间隔闸的 key：按账单（一张账单只有一张聚合码，不需要渠道维度）。
     */
    private static String issueGateKey(Long billId) {
        return ISSUE_GATE_KEY_PREFIX + billId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrPayResultVO queryResult(String payNo) {
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            // 闸3 查单节流：同一张单 5 秒内问第二次纯属重复。被节流就只看本地状态
            if (payRateLimitService.allowsChannelQuery(CHANNEL_QUERY_KEY_PREFIX + payNo, CHANNEL_QUERY_INTERVAL_SECONDS)) {
                settleFromChannel(order);
                order = payOrderService.getByPayNo(payNo);
            }
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            // 过期未付由服务端关单（含渠道关单），前端据此渲染「已过期 + 刷新」；并发付款优先，关不动按最新状态返回
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
    public void mockMarkPaid(String payNo, Integer channel) {
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (!aggregatedQrSupport.isMockEnabled()) {
            throw new BusinessException(
                    "聚合收款处于真实模式（pay.aggregated.mock-enabled=false），拒绝模拟收款：" + payNo);
        }
        // 不传渠道默认微信：模拟的是「患者用微信扫聚合码付了」，要模拟别的渠道传 channel
        Integer ch = channel != null ? channel : PayChannelEnum.WECHAT.getCode();
        aggregatedQrSupport.markMockPaid(payNo, ch, order.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmByAlipayNotify(Map<String, String> params) {
        // 留痕必须拍在验签之前：渠道 SDK 拼签名串时会把传入 Map 里的 sign/sign_type 就地摘掉，
        // 验签之后再序列化，恰好丢掉「它到底拿什么签的」这两个排查伪造通知时最要紧的键
        Map<String, String> rawNotify = params == null ? Map.of() : new LinkedHashMap<>(params);
        String payNo = params == null ? null : params.get("out_trade_no");

        // 闸1 身份：签名由支付宝私钥产出，改一个字节签名就断；再认 app_id，别人的应用签得再对也不是本应用的收款
        if (!alipayChannelService.verifyNotify(params)) {
            log.warn("[支付宝回调·收费侧] 验签未通过，不入账 payNo={}", payNo);
            recordChargeNotify(PayNotifyResultEnum.SIGN_INVALID, NOTIFY_RETRY, false,
                    "验签未通过（签名或 app_id 不符）", rawNotify);
            return NOTIFY_RETRY;
        }
        // 「未付款」的通知（用户取消、交易超时关闭）是合法通知：确认收到即可
        if (!alipayChannelService.isPaidStatus(params.get("trade_status"))) {
            log.info("[支付宝回调·收费侧] 交易未处于已付款状态 payNo={} tradeStatus={}", payNo, params.get("trade_status"));
            recordChargeNotify(PayNotifyResultEnum.NOT_PAID, NOTIFY_ACK, true,
                    "交易状态未达实收：" + TextUtil.trimToEmpty(params.get("trade_status")), rawNotify);
            return NOTIFY_ACK;
        }

        // 闸2 归属：单号必须是我方生成的。
        // 聚合码单出码时渠道为 null（渠道在支付时才定），只要是本应用的通知就认，
        // 已明确登记过渠道（小程序等）的单则必须渠道一致
        PayOrderVO order = payOrderService.getByPayNo(payNo);
        if (order == null) {
            log.error("[支付宝回调·收费侧] 通知里的支付单不存在，疑似跨系统串单 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            recordChargeNotify(PayNotifyResultEnum.ORDER_MISSING, NOTIFY_RETRY, true,
                    "支付单不存在，疑似跨系统串单", rawNotify);
            return NOTIFY_RETRY;
        }
        if (order.getChannel() != null && !Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            // 支付宝通知了一张登记为别的渠道的单：配错回调地址或报文串单，绝不能用支付宝的钱去销微信的账
            log.error("[支付宝回调·收费侧] 支付单渠道不是支付宝，拒入账 payNo={} orderChannel={}",
                    payNo, PayChannelEnum.getLabelOrUnknown(order.getChannel()));
            recordChargeNotify(PayNotifyResultEnum.CHANNEL_MISMATCH, NOTIFY_RETRY, true,
                    "支付单渠道不是支付宝：" + PayChannelEnum.getLabelOrUnknown(order.getChannel()), rawNotify);
            return NOTIFY_RETRY;
        }

        // 闸3 金额格式：total_amount 必须是合法数字
        BigDecimal notifyAmount;
        try {
            notifyAmount = new BigDecimal(TextUtil.trimToEmpty(params.get("total_amount")))
                    .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.error("[支付宝回调·收费侧] total_amount 不是合法金额，不入账 payNo={} raw={}",
                    payNo, params.get("total_amount"));
            recordChargeNotify(PayNotifyResultEnum.AMOUNT_INVALID, NOTIFY_RETRY, true,
                    "total_amount 不是合法金额：" + TextUtil.trimToEmpty(params.get("total_amount")), rawNotify);
            return NOTIFY_RETRY;
        }

        // 回调这一侧没有登录态，收款流水必须写清是谁收的：唯一可归因的事实是「这张码是谁开出来的」
        SystemOperatorContext.bindEmployee(order.getCashierId(), TextUtil.trimToEmpty(order.getCashierName()));
        try {
            // 闸4 幂等翻转 + 金额逐字比对在支付单服务里，与患者端回调共用同一套口径
            PayOrderVO paid = payOrderService.confirmPaid(payNo, PayChannelEnum.ALIPAY.getCode(),
                    notifyAmount, params.get("trade_no"));
            if (paid == null) {
                log.info("[支付宝回调·收费侧] 支付单已入账，重复通知直接应答 payNo={}", payNo);
                recordChargeNotify(PayNotifyResultEnum.DUPLICATED, NOTIFY_ACK, true,
                        "支付单早已入账，本次为重复通知", rawNotify);
                return NOTIFY_ACK;
            }
            bookBill(paid);
            log.info("[支付宝回调·收费侧] 已入账 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            recordChargeNotify(PayNotifyResultEnum.BOOKED, NOTIFY_ACK, true, null, rawNotify);
            return NOTIFY_ACK;
        } catch (RuntimeException e) {
            recordChargeNotify(PayNotifyResultEnum.BOOK_FAILED, NOTIFY_RETRY, true,
                    "入账失败：" + TextUtil.trimToEmpty(e.getMessage()), rawNotify);
            throw e;
        } finally {
            SystemOperatorContext.clear();
        }
    }

    /**
     * 收费台通知出口的留痕收口：报文里能自取的字段全交给工厂方法提，本方只声明「怎么处理到哪一步」。
     */
    private void recordChargeNotify(PayNotifyResultEnum result, String ackBody, boolean signVerified,
                                    String errorMsg, Map<String, String> params) {
        payNotifyLogService.recordNotify(PayNotifyLogService.NotifyRecord.ofAlipay(
                PayNotifySceneEnum.CASHIER_QR, result, ackBody, signVerified, errorMsg, params));
    }

    @Override
    public String confirmByWxNotify(String body) {
        log.error("[微信回调·收费侧] 微信通知通道未接通（Native 出码与 V3 验签解密均未实现），本次通知不受理");
        // 微信 V3 是加密报文，解密前取不出任何字段，原文整体留痕
        payNotifyLogService.recordNotify(PayNotifyLogService.NotifyRecord.ofRawText(
                PayNotifySceneEnum.CASHIER_QR, PayNotifyResultEnum.REJECTED, NOTIFY_RETRY,
                PayChannelEnum.WECHAT.getCode(), false, "微信通知通道未接通，不受理", body));
        return NOTIFY_RETRY;
    }

    /**
     * 渠道查单兜底入账：公网回调断了也要把患者已付的钱记上账。
     */
    private void settleFromChannel(PayOrderVO order) {
        if (order.getChannel() == null) {
            // 聚合码单：渠道在支付时才定，查单走聚合模拟存储（真实模式返回 null，靠网关通知入账）
            AggregatedQrSupport.AggPaidInfo info = aggregatedQrSupport.queryMockPaid(order.getPayNo());
            if (info == null) {
                return;
            }
            confirmAndBook(order, info.channel(), mockTradeNo(order.getPayNo(), info.channel()), info.amount());
            return;
        }
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            // 查单是网络调用，留在本事务里是为了让「翻转 + 记账」保持原子
            AlipayChannelService.TradeState state = alipayChannelService.queryTrade(order.getPayNo());
            if (state == null || !alipayChannelService.isPaidStatus(state.tradeStatus())) {
                return;
            }
            confirmAndBook(order, PayChannelEnum.ALIPAY.getCode(), state.tradeNo(), state.totalAmount());
            return;
        }
        if (Objects.equals(order.getChannel(), PayChannelEnum.WECHAT.getCode())) {
            WxPayChannelService.TradeState state = wxPayChannelService.queryTrade(order.getPayNo());
            if (state == null || !wxPayChannelService.isPaidStatus(state.tradeStatus())) {
                return;
            }
            confirmAndBook(order, PayChannelEnum.WECHAT.getCode(), state.tradeNo(), state.totalAmount());
            return;
        }
        throw new BusinessException("该渠道暂不支持主动查单，请等待渠道回调："
                + PayChannelEnum.getLabelOrUnknown(order.getChannel()));
    }

    private void confirmAndBook(PayOrderVO order, Integer channel, String channelTxnNo, BigDecimal channelAmount) {
        PayOrderVO paid = payOrderService.confirmPaid(order.getPayNo(), channel, channelAmount, channelTxnNo);
        if (paid != null) {
            bookBill(paid);
        }
        log.info("[扫码收款] 查单兜底入账 payNo={} channel={}", order.getPayNo(), channel);
    }

    /**
     * 到账入账：给账单记一笔渠道收款流水（与柜面现金同一个入口，只是来源写「收费台收款」、带上渠道交易号）。
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
                    + PayChannelEnum.getLabelOrUnknown(order.getChannel()));
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

    /**
     * 模拟交易号：与柜面扫码在本地流水上用的拼法一致，便于「渠道账单 ↔ 本地流水」勾对；
     * SIMU- 前缀一眼表明它不是商户平台给出的号。
     */
    private String mockTradeNo(String payNo, Integer channel) {
        return "SIMU-" + channel + "-" + LocalDate.now().format(DateFormats.COMPACT_DATE) + "-" + payNo;
    }

    /**
     * 出码应答：码串本地画成图片。画图是本机 CPU 的活，复用路径到此为止，不再出网。
     *
     * <p>billNo/patientName 是核对基准：前端弹层头部必须改用这两个返回值渲染，
     * 不许继续显示列表行的 props 旧值——否则前端传错 billId 时，
     * 界面显示 A 患者、手里是 B 患者的码，全程静默。
     */
    private QrPayCreateVO toCreateVO(BizSettlementBill bill, PayOrderVO order, String qrCode) {
        String qrImage = PayQrCodeUtils.generateDataUri(qrCode, QR_SIZE);
        if (!TextUtil.hasText(qrImage)) {
            throw new BusinessException("二维码生成失败，请重试");
        }
        log.info("[扫码收款] 出码 payNo={} 账单={} 患者={} 应缴={} 操作人={}",
                order.getPayNo(), bill.getBillNo(), bill.getPatientName(),
                order.getAmount().toPlainString(), UserUtils.getCurrentUser().getRealName());
        QrPayCreateVO vo = new QrPayCreateVO();
        vo.setPayNo(order.getPayNo());
        vo.setChannel(order.getChannel());
        vo.setAmount(order.getAmount());
        vo.setBillNo(bill.getBillNo());
        vo.setPatientName(bill.getPatientName());
        vo.setPayStatus(order.getPayStatus());
        vo.setQrImage(qrImage);
        vo.setExpireTime(order.getExpireTime());
        vo.setServerTime(LocalDateTime.now());
        return vo;
    }

    private BigDecimal unpaidOf(BizSettlementBill bill) {
        return NumUtil.scale(NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount())),
                AMOUNT_SCALE);
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
