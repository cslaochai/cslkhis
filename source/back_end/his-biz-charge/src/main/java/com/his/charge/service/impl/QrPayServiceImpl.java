package com.his.charge.service.impl;

import com.his.api.dto.BillPayDTO;
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
import com.his.api.enums.PayNotifyResultEnum;
import com.his.api.enums.PayNotifySceneEnum;
import com.his.api.enums.PayOrderBizTypeEnum;
import com.his.api.service.AlipayChannelProvider;
import com.his.api.service.PayNotifyLogProvider;
import com.his.api.service.PayOrderProvider;
import com.his.api.service.WxPayChannelProvider;
import com.his.pay.support.PayRateLimitSupport;
import com.his.api.vo.PayOrderVO;
import com.his.system.support.SystemOperatorContext;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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

    /**
     * 同一张账单两次「必须重出码」之间的最小间隔（秒）。
     * 取值口径：要短到收银员觉得等一下不算事，又要长到连点/脚本刷不出量 —— 20 秒内一张码还没扫完是常态，
     * 真的需要重出（患者换手机、扫不上）等 20 秒完全可接受。
     */
    private static final int QR_ISSUE_INTERVAL_SECONDS = 20;

    /**
     * 单个收银员在一个统计窗口内允许的出码次数上限（防的是「换着账单批量刷」，那种刷法按账单限流拦不住）。
     * 正常收款一个人一分钟收不了 30 笔，撞上这条基本就是脚本或在跟谁过不去。
     */
    private static final int QR_ISSUE_QUOTA_PER_WINDOW = 30;

    /**
     * 出码配额的统计窗口（秒）。固定窗口、自第一次出码起算，不滑动：
     * 只为封顶恶意量，不做精细计费，滑动窗口的代价（每次都要重排）不值得。
     */
    private static final int QR_ISSUE_QUOTA_WINDOW_SECONDS = 60;

    /**
     * 同一张支付单两次「真的去渠道查单」之间的最小间隔（秒）。
     * 到账主要靠渠道通知，查单只是公网穿透断了时的兜底，所以查单频率只需覆盖「通知没来」这一种情况。
     */
    private static final int CHANNEL_QUERY_INTERVAL_SECONDS = 5;

    /**
     * 出码间隔闸（同一张账单两次「必须重出码」的最小间隔）、收银员配额闸、查单节流闸，
     * Redis 口径与 fail-open 策略统一收口在 {@link PayRateLimitSupport}，本类只定阈值与 key。
     */
    private static final String ISSUE_GATE_KEY_PREFIX = "his:pay:qr:issue:";
    private static final String ISSUE_QUOTA_KEY_PREFIX = "his:pay:qr:quota:";
    private static final String CHANNEL_QUERY_KEY_PREFIX = "his:pay:qr:query:";

    private final SettlementBillService settlementBillService;

    private final PaymentService paymentService;

    private final PayOrderProvider payOrderProvider;

    private final AlipayChannelProvider alipayChannelProvider;

    private final WxPayChannelProvider wxPayChannelProvider;

    private final PayNotifyLogProvider payNotifyLogProvider;

    private final PayRateLimitSupport payRateLimitSupport;

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

        // 闸1 间隔：命中时只有「原码还能接着用」才放行，且一律本地重画、绝不出网。
        // 为什么不干脆放行建新单：建新单会先把患者手上那张还没扫完的码关掉 —— 换渠道/金额变化的干扰
        // 比多一次请求贵得多，那种情况直接拒绝、让收银员等几秒更安全。
        Long retryAfter = payRateLimitSupport.intervalRetryAfter(
                ISSUE_GATE_KEY_PREFIX + bill.getId(), QR_ISSUE_INTERVAL_SECONDS);
        if (retryAfter != null) {
            PayOrderVO reusable = payOrderProvider.reusablePending(
                    PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode(), bill.getId(), dto.getChannel(), unpaid);
            if (reusable == null) {
                throw new BusinessException("出码过于频繁，请 " + retryAfter + " 秒后再试");
            }
            String cachedQrCode = recallQrCode(reusable);
            if (!TextUtil.hasText(cachedQrCode)) {
                throw new BusinessException("收款码已失效，请 " + retryAfter + " 秒后重新出码");
            }
            return toCreateVO(reusable, cachedQrCode);
        }

        // 闸2 人员配额：按收银员封顶次数，账单维度的间隔闸管不到「换个账单继续刷」
        payRateLimitSupport.assertQuota(ISSUE_QUOTA_KEY_PREFIX + UserUtils.getCurrentUser().getEmployeeId(),
                QR_ISSUE_QUOTA_PER_WINDOW, QR_ISSUE_QUOTA_WINDOW_SECONDS, "出码");

        // 本次没真把码出来就得把间隔闸的窗口退回去（见 PayRateLimitSupport.release）：
        // 建单、出码、画图任一步失败都不能让收银员对着「过于频繁」的提示干等 20 秒，那是在替他掩盖真故障
        try {
            PayOrderProvider.CreateResult created = payOrderProvider.createPending(new PayOrderProvider.CreateSpec(
                    PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode(), bill.getId(),
                    bill.getPatientId(), bill.getPatientName(), unpaid, dto.getChannel(),
                    "收费台扫码收款 账单 " + bill.getBillNo()));
            PayOrderVO order = created.order();

            // 复用命中的单：码串在渠道侧本来就还是那一句话，取回缓存本地画图，不再打渠道
            String qrCode = created.reused() ? recallQrCode(order) : null;
            if (!TextUtil.hasText(qrCode)) {
                WxPayChannelProvider.PayUnifiedResult unified = precreateOfChannel(order);
                if (unified.errMsg() != null) {
                    throw new BusinessException(unified.errMsg());
                }
                qrCode = unified.payParams().get("qrCode");
            }

            return toCreateVO(order, qrCode);
        } catch (RuntimeException e) {
            payRateLimitSupport.release(ISSUE_GATE_KEY_PREFIX + bill.getId());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QrPayResultVO queryResult(String payNo) {
        PayOrderVO order = payOrderProvider.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            // 闸3 查单节流：轮询间隔由前端决定（页面多开时是成倍放大），但渠道侧状态不会秒级变，
            // 同一张单 5 秒内问第二次纯属重复。被节流就只看本地状态 —— 钱真到了通知会把它翻过来。
            if (payRateLimitSupport.allowsChannelQuery(
                    CHANNEL_QUERY_KEY_PREFIX + payNo, CHANNEL_QUERY_INTERVAL_SECONDS)) {
                settleFromChannel(order);
                order = payOrderProvider.getByPayNo(payNo);
            }
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PENDING.getCode())) {
            // 过期未付由服务端关单（含渠道关单），前端据此渲染「已过期 + 刷新」；
            // 并发付款的钱优先，关不动就按最新状态返回
            payOrderProvider.closeExpiredIfPending(payNo);
            order = payOrderProvider.getByPayNo(payNo);
        }
        return toResultVO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(String payNo) {
        payOrderProvider.closePending(payNo);
    }

    @Override
    public void mockMarkPaid(String payNo) {
        PayOrderVO order = payOrderProvider.getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            throw new BusinessException("仅支付宝出码支持模拟收款，当前渠道："
                    + PayChannelEnum.labelOrUnknown(order.getChannel()));
        }
        // 金额取支付单自己的应缴额：模拟的是「患者照单全付」，要模拟少付就得改传参，
        // 而那条路径该由真渠道或退费流程覆盖，不在这里开口子
        alipayChannelProvider.markMockPaid(payNo, order.getAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmByAlipayNotify(Map<String, String> params) {
        // 留痕必须拍在验签之前：渠道 SDK 拼签名串时会把传入 Map 里的 sign/sign_type 就地摘掉，
        // 验签之后再序列化，恰好丢掉「它到底拿什么签的」这两个排查伪造通知时最要紧的键
        Map<String, String> rawNotify = params == null ? Map.of() : new LinkedHashMap<>(params);
        String payNo = params == null ? null : params.get("out_trade_no");

        // 闸1 身份：签名由支付宝私钥产出，改一个字节签名就断；再认 app_id，别人的应用签得再对也不是本应用的收款
        if (!alipayChannelProvider.verifyNotify(params)) {
            log.warn("[支付宝回调·收费侧] 验签未通过，不入账 payNo={}", payNo);
            recordChargeNotify(PayNotifyResultEnum.SIGN_INVALID, NOTIFY_RETRY, false,
                    "验签未通过（签名或 app_id 不符）", rawNotify);
            return NOTIFY_RETRY;
        }
        // 「未付款」的通知（用户取消、交易超时关闭）是合法通知：确认收到即可，
        // 回 failure 会让支付宝对一条永远不会变成已付款的通知重试整天
        if (!alipayChannelProvider.isPaidStatus(params.get("trade_status"))) {
            log.info("[支付宝回调·收费侧] 交易未处于已付款状态 payNo={} tradeStatus={}", payNo, params.get("trade_status"));
            recordChargeNotify(PayNotifyResultEnum.NOT_PAID, NOTIFY_ACK, true,
                    "交易状态未达实收：" + TextUtil.trimToEmpty(params.get("trade_status")), rawNotify);
            return NOTIFY_ACK;
        }

        // 闸2 归属：单号必须是我方生成的，且通知渠道与本单登记的渠道一致
        PayOrderVO order = payOrderProvider.getByPayNo(payNo);
        if (order == null) {
            log.error("[支付宝回调·收费侧] 通知里的支付单不存在，疑似跨系统串单 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            recordChargeNotify(PayNotifyResultEnum.ORDER_MISSING, NOTIFY_RETRY, true,
                    "支付单不存在，疑似跨系统串单", rawNotify);
            return NOTIFY_RETRY;
        }
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            // 支付宝通知了一张登记为别的渠道的单：配错回调地址或报文串单，绝不能用支付宝的钱去销微信的账
            log.error("[支付宝回调·收费侧] 支付单渠道不是支付宝，拒入账 payNo={} orderChannel={}",
                    payNo, PayChannelEnum.labelOrUnknown(order.getChannel()));
            recordChargeNotify(PayNotifyResultEnum.CHANNEL_MISMATCH, NOTIFY_RETRY, true,
                    "支付单渠道不是支付宝：" + PayChannelEnum.labelOrUnknown(order.getChannel()), rawNotify);
            return NOTIFY_RETRY;
        }

        // 闸3 金额格式：total_amount 必须是合法数字。注意这只判「报文是不是脏数字」，
        // 金额是否与本单逐字相等仍由支付单闸门（confirmPaid）判，数值不符留给对账人工处理，绝不按本地金额兜底入账
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

        // 回调这一侧没有登录态，而收款流水必须写清是谁收的：唯一可归因的事实是「这张码是谁开出来的」，
        // 出码人在支付单的创建人字段上（取自我方库，不是通知报文里的字段）。
        // 绑定放在几道拒收判定之后：验签失败/串单/渠道不符/金额非法都不会入账，不必也不该伪造操作人
        SystemOperatorContext.bindEmployee(order.getCashierId(), TextUtil.trimToEmpty(order.getCashierName()));
        try {
            // 闸4 幂等翻转 + 金额逐字比对在支付单服务里，与患者端回调共用同一套口径
            PayOrderVO paid = payOrderProvider.confirmPaid(payNo, PayChannelEnum.ALIPAY.getCode(),
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
            // 入账失败必须先把这条通知钉在留痕里再抛：外层事务会把状态改动全洗掉，
            // 而「渠道说钱到了、本地没记上」正是事后最难复现的一种账，光靠日志翻不回当时那份报文
            recordChargeNotify(PayNotifyResultEnum.BOOK_FAILED, NOTIFY_RETRY, true,
                    "入账失败：" + TextUtil.trimToEmpty(e.getMessage()), rawNotify);
            throw e;
        } finally {
            SystemOperatorContext.clear();
        }
    }

    /**
     * 收费台通知出口的留痕收口：报文里能自取的字段全交给工厂方法提，本方只声明「怎么处理到哪一步」，
     * 与患者端 {@code MiniPayServiceImpl} 的同名收口同构，避免两个出口各抄一遍键名导致口径漂移。
     */
    private void recordChargeNotify(PayNotifyResultEnum result, String ackBody, boolean signVerified,
                                    String errorMsg, Map<String, String> params) {
        payNotifyLogProvider.recordNotify(PayNotifyLogProvider.NotifyRecord.ofAlipay(
                PayNotifySceneEnum.CASHIER_QR, result, ackBody, signVerified, errorMsg, params));
    }

    @Override
    public String confirmByWxNotify(String body) {
        log.error("[微信回调·收费侧] 微信通知通道未接通（Native 出码与 V3 验签解密均未实现），本次通知不受理");
        // 微信 V3 是加密报文，解密前取不出任何字段，原文整体留痕（这也是将来接真实现时唯一的排查素材来源）
        payNotifyLogProvider.recordNotify(PayNotifyLogProvider.NotifyRecord.ofRawText(
                PayNotifySceneEnum.CASHIER_QR, PayNotifyResultEnum.REJECTED, NOTIFY_RETRY,
                PayChannelEnum.WECHAT.getCode(), false, "微信通知通道未接通，不受理", body));
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
        AlipayChannelProvider.TradeState state = alipayChannelProvider.queryTrade(order.getPayNo());
        if (state == null || !alipayChannelProvider.isPaidStatus(state.tradeStatus())) {
            return;
        }
        PayOrderVO paid = payOrderProvider.confirmPaid(order.getPayNo(), PayChannelEnum.ALIPAY.getCode(),
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

    private WxPayChannelProvider.PayUnifiedResult precreateOfChannel(PayOrderVO order) {
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            return alipayChannelProvider.precreate(order);
        }
        if (Objects.equals(order.getChannel(), PayChannelEnum.WECHAT.getCode())) {
            return wxPayChannelProvider.nativePrecreate(order);
        }
        throw new BusinessException("该渠道暂不支持出码收款：" + PayChannelEnum.labelOrUnknown(order.getChannel()));
    }

    /**
     * 取回这张单已出过的码串（本地重画用），渠道没有「按单号回想」能力的一律返回 null 走真出码。
     */
    private String recallQrCode(PayOrderVO order) {
        if (Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            return alipayChannelProvider.recallQrCode(order.getPayNo());
        }
        return null;
    }

    /**
     * 出码应答：码串本地画成图片。画图是本机 CPU 的活，复用路径到此为止，不再出网。
     */
    private QrPayCreateVO toCreateVO(PayOrderVO order, String qrCode) {
        String qrImage = PayQrCodeUtils.generateDataUri(qrCode, QR_SIZE);
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
