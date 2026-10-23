package com.his.app.service.impl;

import com.his.common.support.SystemOperatorContext;
import com.his.common.utils.UserUtils;
import com.his.spi.service.RegistFacadeService;
import com.his.spi.vo.RegistBriefVO;
import com.his.spi.dto.BillPayDTO;
import com.his.spi.dto.PrepayUpsertDTO;
import com.his.spi.dto.PayOrderCreateDTO;
import com.his.spi.dto.ChannelNotifyRecordDTO;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.service.PayChannelFacadeService;
import com.his.spi.service.PayOrderFacadeService;
import com.his.spi.service.PayRateLimitFacadeService;
import com.his.spi.vo.PayOrderCreateResultVO;
import com.his.spi.vo.PayOrderInfoVO;
import com.his.spi.vo.MiniPayUnifiedResultVO;
import com.his.spi.vo.ChannelTradeStateVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.app.dto.PayRefundDTO;
import com.his.app.dto.PayUpsertDTO;
import com.his.app.service.MiniPayService;
import com.his.app.vo.MiniPayOrderListVO;
import com.his.app.vo.MiniPayOrderVO;
import com.his.app.vo.MiniPendingBillItemVO;
import com.his.app.vo.MiniPendingBillListVO;
import com.his.common.enums.PayNotifyResultEnum;
import com.his.common.enums.PayNotifySceneEnum;
import com.his.common.enums.PayOrderBizTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 患者端聚合服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniPayServiceImpl implements MiniPayService {

    /**
     * 渠道协议应答原文（不是本库码值）：支付宝只看 body 是否等于 success，其余一律按失败重试。
     */
    private static final String NOTIFY_ACK = "success";
    private static final String NOTIFY_RETRY = "failure";

    private static final int AMOUNT_SCALE = 2;
    private static final String DEFAULT_REFUND_REASON = "患者退号";

    /**
     * 「我的支付单」一次取回的最近条数（缴费记录展示，非分页查询）
     */
    private static final int MY_ORDER_LIMIT = 50;

    /**
     * 单个患者在一个统计窗口内允许的下单次数上限（防的是连点 / 脚本反复调起渠道收银台）。
     */
    private static final int ORDER_QUOTA_PER_WINDOW = 20;
    private static final int ORDER_QUOTA_WINDOW_SECONDS = 60;

    /**
     * 同一张支付单两次「真的去渠道查单」之间的最小间隔（秒），与收费台同口径。
     */
    private static final int CHANNEL_QUERY_INTERVAL_SECONDS = 5;

    private static final String ORDER_QUOTA_KEY_PREFIX = "his:pay:mini:quota:";
    private static final String CHANNEL_QUERY_KEY_PREFIX = "his:pay:mini:query:";

    private final PayOrderFacadeService payOrderFacadeService;

    private final PayChannelFacadeService payChannelFacadeService;

    private final PayRateLimitFacadeService payRateLimitFacadeService;

    private final ChargeFacadeService chargeFacadeService;

    private final RegistFacadeService registFacadeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniPayOrderVO createOrder(PayUpsertDTO dto) {
        var current = UserUtils.getCurrentUser();
        // 幂等查重：已收过款的业务单不再建第二张支付单
        PayOrderInfoVO alreadyPaid = payOrderFacadeService.findLatestPaid(dto.getBizType(), dto.getBizId());
        if (alreadyPaid != null) {
            throw new BusinessException("该账单已支付，请勿重复付款");
        }
        payRateLimitFacadeService.assertQuota(ORDER_QUOTA_KEY_PREFIX + current.getPatientId(),
                ORDER_QUOTA_PER_WINDOW, ORDER_QUOTA_WINDOW_SECONDS, "下单");

        PayOrderCreateDTO spec = new PayOrderCreateDTO();
        spec.setBizType(dto.getBizType());
        spec.setBizId(dto.getBizId());
        spec.setPatientId(current.getPatientId());
        spec.setPatientName(current.getRealName());
        spec.setAmount(resolveAmount(dto));
        spec.setChannel(dto.getChannel());
        spec.setRemark("患者端小程序支付");
        PayOrderCreateResultVO created = payOrderFacadeService.createPending(spec);
        PayOrderInfoVO order = created.getOrder();

        // JSAPI 预下单：取微信 wx.requestPayment 参数 / 支付宝表单串；渠道没出参数时 payParams 为空，
        // 前端拉起渠道前不得据此置成功，收款结果只来自渠道异步通知或主动查单。
        MiniPayUnifiedResultVO unified = payChannelFacadeService.unifiedJsapiOrder(order, dto.getChannel());
        Map<String, String> payParams = unified.getPayParams();
        if (payParams == null && TextUtil.hasText(unified.getErrMsg())) {
            log.warn("[患者端支付] JSAPI 预下单未出参数 payNo={} channel={} err={}",
                    order.getPayNo(), order.getChannel(), unified.getErrMsg());
        }
        return toVO(order, payParams);
    }

    @Override
    public List<MiniPayOrderListVO> myOrders() {
        var current = UserUtils.getCurrentUser();
        return payOrderFacadeService.listRecentByPatient(current.getPatientId(), MY_ORDER_LIMIT)
                .stream().map(this::toListVO).toList();
    }

    @Override
    public List<MiniPendingBillListVO> pendingBills() {
        var current = UserUtils.getCurrentUser();
        return chargeFacadeService.pendingBills(current.getPatientId()).stream().map(view -> {
            MiniPendingBillListVO vo = new MiniPendingBillListVO();
            vo.setId(view.getId());
            vo.setBillNo(view.getBillNo());
            vo.setEncounterNo(view.getEncounterNo());
            vo.setPayableAmount(view.getPayableAmount());
            vo.setBillTime(view.getBillTime());
            vo.setBillStatus(view.getBillStatus());
            vo.setPoolAmount(view.getPoolAmount());
            vo.setAccountAmount(view.getAccountAmount());
            vo.setSelfAmount(view.getSelfAmount());
            vo.setDetails(toPendingBillItems(view.getDetails()));
            return vo;
        }).toList();
    }

    /**
     * his-charge 的账单明细 → 患者端明细行。
     *
     * <p>入参保持全限定名：它是收费域的账本形状，出参是患者端自己维护的展示契约，
     * 两边字段同形但归属不同模块，各改各的。
     */
    private List<MiniPendingBillItemVO> toPendingBillItems(List<com.his.spi.vo.PendingBillItemVO> details) {
        if (details == null) {
            return List.of();
        }
        return details.stream().map(item -> {
            MiniPendingBillItemVO vo = new MiniPendingBillItemVO();
            vo.setItemName(item.getItemName());
            vo.setAmount(item.getAmount());
            vo.setDeptName(item.getDeptName());
            vo.setSpecification(item.getSpecification());
            vo.setUnit(item.getUnit());
            vo.setPrice(item.getPrice());
            vo.setQuantity(item.getQuantity());
            vo.setPoolAmount(item.getPoolAmount());
            vo.setAccountAmount(item.getAccountAmount());
            vo.setSelfAmount(item.getSelfAmount());
            vo.setCatalogType(item.getCatalogType());
            vo.setCatalogTypeText(FeeCatalogTypeEnum.getLabel(item.getCatalogType()));
            return vo;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundByBiz(PayRefundDTO dto) {
        // 渠道原路退回由退号/退费编排完成，这里只把本地已支付单置已退款；无已支付单幂等跳过
        PayOrderInfoVO paid = payOrderFacadeService.findLatestPaid(dto.getBizType(), dto.getBizId());
        if (paid == null) {
            return;
        }
        String reason = TextUtil.blankToDefault(dto.getReason(), DEFAULT_REFUND_REASON);
        payOrderFacadeService.markRefunded(paid.getPayNo(), reason);
        log.info("[患者端支付] 已支付单置已退款 payNo={} bizType={} bizId={}",
                paid.getPayNo(), dto.getBizType(), dto.getBizId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmByAlipayNotify(Map<String, String> params) {
        Map<String, String> rawNotify = params == null ? Map.of() : new LinkedHashMap<>(params);
        String payNo = params == null ? null : params.get("out_trade_no");

        // 闸1 身份：签名由支付宝私钥产出，改一个字节签名就断；再认 app_id，别人的应用签得再对也不是本应用的收款
        if (!payChannelFacadeService.verifyAlipayNotify(params)) {
            log.warn("[支付宝回调] 验签未通过，不入账 payNo={}", payNo);
            recordAlipayNotify(PayNotifyResultEnum.SIGN_INVALID, NOTIFY_RETRY, false,
                    "验签未通过（签名或 app_id 不符）", rawNotify);
            return NOTIFY_RETRY;
        }

        // 「未付款」的通知（用户取消、交易超时关闭）是合法通知：确认收到即可。
        // 这里若回 failure，支付宝会对一条永远不会变成已付款的通知按策略重试整天。
        if (!payChannelFacadeService.isAlipayPaidStatus(params.get("trade_status"))) {
            log.info("[支付宝回调] 交易未处于已付款状态，无需入账 payNo={} tradeStatus={}", payNo, params.get("trade_status"));
            recordAlipayNotify(PayNotifyResultEnum.NOT_PAID, NOTIFY_ACK, true,
                    "交易状态未达实收：" + TextUtil.trimToEmpty(params.get("trade_status")), rawNotify);
            return NOTIFY_ACK;
        }

        // 闸2 归属：单号必须是我方生成的，且渠道与本单登记的一致
        PayOrderInfoVO order = payOrderFacadeService.getByPayNo(payNo);
        if (order == null) {
            log.error("[支付宝回调] 通知里的支付单不存在，疑似跨系统串单 payNo={} tradeNo={}", payNo, params.get("trade_no"));
            recordAlipayNotify(PayNotifyResultEnum.ORDER_MISSING, NOTIFY_RETRY, true,
                    "支付单不存在，疑似跨系统串单", rawNotify);
            return NOTIFY_RETRY;
        }
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            log.error("[支付宝回调] 支付单渠道不是支付宝，拒入账 payNo={} orderChannel={}",
                    payNo, PayChannelEnum.getLabelOrUnknown(order.getChannel()));
            recordAlipayNotify(PayNotifyResultEnum.CHANNEL_MISMATCH, NOTIFY_RETRY, true,
                    "支付单渠道不是支付宝：" + PayChannelEnum.getLabelOrUnknown(order.getChannel()), rawNotify);
            return NOTIFY_RETRY;
        }

        // 闸4 金额：支付宝签的总额必须与本库应缴逐字相等，比对与翻转在支付单闸门内做。
        // 不符时入账等于「付一份钱、销多笔账」，这类单留给对账人工处理，绝不做「按本地金额入账」的兜底。
        BigDecimal notifyAmount;
        try {
            notifyAmount = new BigDecimal(TextUtil.trimToEmpty(params.get("total_amount")));
        } catch (NumberFormatException e) {
            log.error("[支付宝回调] total_amount 不是合法金额，不入账 payNo={} raw={}", payNo, params.get("total_amount"));
            recordAlipayNotify(PayNotifyResultEnum.AMOUNT_INVALID, NOTIFY_RETRY, true,
                    "total_amount 不是合法金额：" + TextUtil.trimToEmpty(params.get("total_amount")), rawNotify);
            return NOTIFY_RETRY;
        }

        // 渠道回调是无登录态入口，而收款流水必须写清是谁付的：唯一可归因的事实是「这张单是谁建的」。
        // 按患者身份绑（不是员工），这笔钱才会落进日结的「系统代收」桶而不是某个收银员的班结
        SystemOperatorContext.bindPatient(order.getCashierId(), order.getPatientId(),
                TextUtil.trimToEmpty(order.getCashierName()));
        try {
            boolean booked = settlePaid(payNo, PayChannelEnum.ALIPAY.getCode(), notifyAmount, params.get("trade_no"));
            recordAlipayNotify(booked ? PayNotifyResultEnum.BOOKED : PayNotifyResultEnum.DUPLICATED,
                    NOTIFY_ACK, true, booked ? null : "支付单早已入账，本次为重复通知", rawNotify);
            log.info("[支付宝回调] 已受理 payNo={} tradeNo={} amount={}",
                    payNo, params.get("trade_no"), notifyAmount.toPlainString());
            return NOTIFY_ACK;
        } catch (RuntimeException e) {
            // 入账失败必须先把这条通知钉在留痕里再抛：外层事务会把状态改动全洗掉，
            // 而「渠道说钱到了、本地没记上」正是事后最难复现的一种账，光靠日志翻不回当时那份报文
            recordAlipayNotify(PayNotifyResultEnum.BOOK_FAILED, NOTIFY_RETRY, true,
                    "入账失败：" + TextUtil.trimToEmpty(e.getMessage()), rawNotify);
            throw e;
        } finally {
            SystemOperatorContext.clear();
        }
    }

    /**
     * 患者端通知出口的留痕收口：报文里能自取的字段全交给实现方提，本方只声明「怎么处理到哪一步」。
     */
    private void recordAlipayNotify(PayNotifyResultEnum result, String ackBody, boolean signVerified,
                                    String errorMsg, Map<String, String> params) {
        ChannelNotifyRecordDTO dto = new ChannelNotifyRecordDTO();
        dto.setNotifyScene(PayNotifySceneEnum.PATIENT_MINI.getCode());
        dto.setNotifyResult(result.getCode());
        dto.setAckBody(ackBody);
        dto.setSignVerified(signVerified);
        dto.setErrorMsg(errorMsg);
        dto.setNotifyParams(params);
        payChannelFacadeService.recordChannelNotify(dto);
    }

    @Override
    public String confirmByWxNotify(String body) {
        log.error("[微信回调] 微信通知通道未接通（JSAPI 下单与 V3 验签解密均未实现），本次通知不受理");
        // 微信 V3 是加密报文，解密前取不出任何字段，原文整体留痕（这也是将来接真实现时唯一的排查素材来源）
        ChannelNotifyRecordDTO dto = new ChannelNotifyRecordDTO();
        dto.setNotifyScene(PayNotifySceneEnum.PATIENT_MINI.getCode());
        dto.setNotifyResult(PayNotifyResultEnum.REJECTED.getCode());
        dto.setAckBody(NOTIFY_RETRY);
        dto.setChannel(PayChannelEnum.WECHAT.getCode());
        dto.setSignVerified(false);
        dto.setErrorMsg("微信通知通道未接通，不受理");
        dto.setRawBody(body);
        payChannelFacadeService.recordChannelNotify(dto);
        return NOTIFY_RETRY;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniPayOrderVO syncFromChannel(String payNo) {
        Long patientId = UserUtils.getCurrentUser().getPatientId();
        PayOrderInfoVO order = payOrderFacadeService.getByPayNo(payNo);
        // 越权与不存在同答：分开报等于告诉调用方「这个单号是真的」，支付单号可枚举
        if (order == null || !Objects.equals(order.getPatientId(), patientId)) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (!Objects.equals(order.getChannel(), PayChannelEnum.ALIPAY.getCode())) {
            throw new BusinessException("该渠道暂不支持主动查单，请稍后刷新支付状态："
                    + PayChannelEnum.getLabelOrUnknown(order.getChannel()));
        }
        if (Objects.equals(order.getPayStatus(), PayOrderStatusEnum.PAID.getCode())) {
            return toVO(order, null);
        }

        if (!payRateLimitFacadeService.allowsChannelQuery(
                CHANNEL_QUERY_KEY_PREFIX + payNo, CHANNEL_QUERY_INTERVAL_SECONDS)) {
            return toVO(order, null);
        }

        ChannelTradeStateVO state = payChannelFacadeService.queryAlipayTrade(payNo);
        if (state == null || !payChannelFacadeService.isAlipayPaidStatus(state.getTradeStatus())) {
            log.info("[主动查单] 渠道侧尚未确认收款 payNo={} state={}", payNo, state);
            return toVO(order, null);
        }
        settlePaid(payNo, PayChannelEnum.ALIPAY.getCode(), state.getTotalAmount(), state.getTradeNo());
        log.info("[主动查单] 渠道已确认收款 payNo={} tradeNo={}", payNo, state.getTradeNo());
        return toVO(payOrderFacadeService.getByPayNo(payNo), null);
    }

    /**
     * 确认实收并推进业务：翻转（条件更新，只允许成功一次）与推进必须在同一事务里，
     * 否则会出现「钱已记成实收、账还停在待支付」的永久漏账。
     *
     * @return 本次是否真的推进了业务；false = 这条通知重复到达（或并发那一路先赢），推进已由赢家做完
     */
    private boolean settlePaid(String payNo, Integer channel, BigDecimal channelAmount, String channelTxnNo) {
        PayOrderInfoVO paid = payOrderFacadeService.confirmPaid(payNo, channel, channelAmount, channelTxnNo);
        if (paid == null) {
            log.info("[患者端支付] 支付单已入账，跳过推进 payNo={}", payNo);
            return false;
        }
        advance(paid);
        return true;
    }

    private void advance(PayOrderInfoVO order) {
        Integer bizType = order.getBizType();
        if (Objects.equals(bizType, PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode())) {
            payBill(order.getBizId(), order, "门诊缴费");
        } else if (Objects.equals(bizType, PayOrderBizTypeEnum.REGISTER_FEE.getCode())) {
            payRegistBill(order);
        } else if (Objects.equals(bizType, PayOrderBizTypeEnum.INPATIENT_DEPOSIT.getCode())) {
            rechargeDeposit(order);
        } else {
            throw new BusinessException("未知业务类型：" + PayOrderBizTypeEnum.getLabelOrUnknown(bizType));
        }
    }

    /**
     * 2-挂号费：给挂号账单记一笔渠道收款流水（到院补缴口径的正向链）。
     *
     * <p>挂号 upsert 时收费域已出「1-挂号费结算」账单并回写 bill_id，签到硬校验账单是否付清 ——
     * 所以患者端支付成功必须落到支付资金流水，否则患者线上付了钱到院仍签不了到。
     */
    private void payRegistBill(PayOrderInfoVO order) {
        RegistBriefVO regist = registFacadeService.findRegist(order.getBizId());
        if (regist == null) {
            throw new BusinessException("挂号记录不存在：" + order.getBizId());
        }
        if (regist.getBillId() == null) {
            // 免收/老数据场景：没有账单就没有该收多少钱这件事，支付单仅留档，不凭空造账单
            log.info("[患者端支付] 挂号费入账 payNo={} registId={}（无关联账单，凭证留档）",
                    order.getPayNo(), order.getBizId());
            return;
        }
        payBill(regist.getBillId(), order, "挂号费");
    }

    private BigDecimal resolveAmount(PayUpsertDTO dto) {
        // 门诊缴费：金额一律以账单剩余应缴为准（前端传值忽略，防止单据与支付金额不一致）
        if (Objects.equals(dto.getBizType(), PayOrderBizTypeEnum.OUTPATIENT_PAY.getCode())) {
            BigDecimal unpaid = chargeFacadeService.unpaidAmount(dto.getBizId());
            if (unpaid == null) {
                throw new BusinessException("结算账单不存在：" + dto.getBizId());
            }
            return unpaid;
        }
        // 挂号费/押金：以患者端传值为准
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支付金额必须大于0");
        }
        return NumUtil.scale(dto.getAmount(), AMOUNT_SCALE);
    }

    /**
     * 支付成功 → 账单收款（L3 一笔流水，与院内收费台同一入口，只是流水来源写「患者端支付」）。
     *
     * <p>金额取<b>账单剩余应缴</b>而不是支付单上的 amount：患者端页面停留期间窗口可能已经收过一笔，
     * 按页面金额收就是多收，多收的那部分是要再走退费流程的真钱。
     * 已无应缴时只留档不报错 —— 钱是渠道已收的事实，但本院账上不缺这笔，重复收款交给日结勾对。
     */
    private void payBill(Long billId, PayOrderInfoVO order, String scene) {
        BigDecimal unpaid = chargeFacadeService.unpaidAmount(billId);
        if (unpaid == null) {
            throw new BusinessException(scene + "推进失败：结算账单不存在（" + billId + "）");
        }
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[患者端支付] {} 账单 {} 已无应缴（可能窗口已收），支付单仅留档 payNo={}",
                    scene, billId, order.getPayNo());
            return;
        }
        PaymentMethodEnum payMethod = payMethodOfChannel(order.getChannel());
        BillPayDTO dto = new BillPayDTO();
        dto.setBillId(billId);
        dto.setSourceType(TxnSourceEnum.MINIAPP.getCode());
        BillPayDTO.PayItem item = new BillPayDTO.PayItem();
        item.setPayMethod(payMethod.getCode());
        item.setAmount(unpaid);
        item.setChannelTxnNo(order.getOutTradeNo());
        item.setRemark(scene + "：患者端" + PayChannelEnum.getLabel(order.getChannel()) + "支付 支付单 " + order.getPayNo());
        dto.setItems(List.of(item));
        chargeFacadeService.payBill(dto);
        log.info("[患者端支付] {}已入账账单 payNo={} billId={} 渠道={} amount=￥{}",
                scene, order.getPayNo(), billId, payMethod.getDesc(), unpaid.toPlainString());
    }

    /**
     * 押金推进：走住院预交金充值入口，余额与院内同一套流水、同一个账户。
     *
     * <p>渠道交易号必须带上：日后原路退回要靠它向渠道发起退款，不带就只能退现金，
     * 收银台当天抽屉里会多出一笔说不清的现金。
     */
    private void rechargeDeposit(PayOrderInfoVO order) {
        PaymentMethodEnum payMethod = payMethodOfChannel(order.getChannel());
        PrepayUpsertDTO prepay = new PrepayUpsertDTO();
        prepay.setAdmissionId(order.getBizId());
        // 预交金流水类型与资金方向同码值（1-充值=收款、2-退款=退款），不另建一套码
        prepay.setPrepayType(PayDirectionEnum.CHARGE.getCode());
        prepay.setAmount(order.getAmount());
        prepay.setPayMethod(payMethod.getCode());
        prepay.setChannelTxnNo(order.getOutTradeNo());
        prepay.setPayTime(order.getPayTime());
        prepay.setRemark("患者端" + PayChannelEnum.getLabel(order.getChannel()) + "充值（支付单 " + order.getPayNo() + "）");
        chargeFacadeService.savePrepay(prepay);
        log.info("[患者端支付] 住院押金入账 payNo={} admissionId={} 渠道={} amount=￥{}",
                order.getPayNo(), order.getBizId(), payMethod.getDesc(), order.getAmount().toPlainString());
    }

    /**
     * 渠道 → 收款方式。映射不上就拒绝入账，绝不兜成现金：
     * 兜成现金等于把「第三方收的钱」记进收银员的抽屉，日结必然多出一笔说不清的现金。
     */
    private PaymentMethodEnum payMethodOfChannel(Integer channel) {
        PaymentMethodEnum payMethod = PayChannelEnum.payMethodOf(channel);
        if (payMethod == null) {
            throw new BusinessException("支付渠道无法映射收款方式，不入账：" + PayChannelEnum.getLabelOrUnknown(channel));
        }
        return payMethod;
    }

    private MiniPayOrderVO toVO(PayOrderInfoVO order, Map<String, String> payParams) {
        MiniPayOrderVO vo = new MiniPayOrderVO();
        vo.setPayNo(order.getPayNo());
        vo.setPayOrderId(order.getId());
        vo.setPayStatus(order.getPayStatus());
        vo.setAmount(order.getAmount());
        vo.setPayParams(payParams);
        return vo;
    }

    private MiniPayOrderListVO toListVO(PayOrderInfoVO order) {
        MiniPayOrderListVO vo = new MiniPayOrderListVO();
        vo.setId(order.getId());
        vo.setPayNo(order.getPayNo());
        vo.setBizType(order.getBizType());
        vo.setBizId(order.getBizId());
        vo.setAmount(order.getAmount());
        vo.setPayStatus(order.getPayStatus());
        vo.setPayTime(order.getPayTime());
        return vo;
    }
}
