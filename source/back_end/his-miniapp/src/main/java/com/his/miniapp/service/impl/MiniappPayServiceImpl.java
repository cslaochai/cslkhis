package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.service.BizAppointService;
import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.PrepayUpsertDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.enums.TxnSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.dto.PayUpsertDTO;
import com.his.miniapp.dto.WxLoginDTO;
import com.his.miniapp.entity.BizPayOrder;
import com.his.miniapp.mapper.BizPayOrderMapper;
import com.his.miniapp.mapper.MiniappSysUserMapper;
import com.his.miniapp.service.MiniappPayService;
import com.his.miniapp.service.WxLoginChannelService;
import com.his.miniapp.service.WxPayChannelService;
import com.his.miniapp.vo.*;
import com.his.system.service.SysMessageService;
import com.his.system.utils.JwtUtils;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 患者端聚合服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappPayServiceImpl extends ServiceImpl<BizPayOrderMapper, BizPayOrder> implements MiniappPayService {

    private final WxLoginChannelService wxLoginChannelService;
    private final WxPayChannelService wxPayChannelService;
    private final BizPayOrderMapper bizPayOrderMapper;
    private final MiniappSysUserMapper miniappSysUserMapper;
    private final JwtUtils jwtUtils;
    private final RedisSequenceService redisSequenceService;
    private final SettlementBillService settlementBillService;
    private final PaymentService paymentService;
    private final InpatientAccountService inpatientAccountService;
    private final SysMessageService sysMessageService;
    private final BizAppointService bizAppointService;

    // 微信登录口子
    @Override
    public WxLoginVO wxLogin(WxLoginDTO dto) {
        String openid = wxLoginChannelService.code2Session(dto.getCode());
        MiniappUserRowVO user = miniappSysUserMapper.selectByOpenid(openid);
        WxLoginVO vo = new WxLoginVO();
        vo.setBound(false);
        if (user == null) {
            // 未绑定：前端引导账密/短信注册登录后调 /miniapp/auth/bindOpenid
            return vo;
        }
        if (!Integer.valueOf(3).equals(user.getUserType())) {
            throw new BusinessException("该微信已绑定院内员工账号，患者端不支持该方式登录");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已停用，请联系医院");
        }
        Long userId = user.getId();
        String username = user.getUserName();
        String realName = user.getRealName() == null ? username : user.getRealName();
        Long patientId = user.getPatientId();

        String token = jwtUtils.generateToken(userId, username, "PATIENT", null, null);
        vo.setBound(true);
        vo.setToken(token);
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setRealName(realName);
        vo.setPatientId(patientId);
        vo.setUserType(3);
        return vo;
    }

    // 统一支付单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderVO createOrder(PayUpsertDTO dto) {
        // 幂等：同业务单已有待支付/已支付的有效单，直接返回它（防重复拉起收银台）
        BizPayOrder exist = bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, dto.getBizType())
                .eq(BizPayOrder::getBizId, dto.getBizId())
                .in(BizPayOrder::getPayStatus, 0, 1)
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1"));
        if (exist != null && exist.getPayStatus() != null && exist.getPayStatus() == 1) {
            return toVO(exist, null);
        }

        var current = UserUtils.getCurrentUser();
        BizPayOrder order = new BizPayOrder();
        order.setPayNo("PAY" + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%05d", redisSequenceService.next("PAY")));
        order.setBizType(dto.getBizType());
        order.setBizId(dto.getBizId());
        order.setPatientId(current.getPatientId());
        order.setPatientName(current.getRealName());
        order.setAmount(resolveAmount(dto));
        order.setChannel(1);
        order.setPayStatus(0);
        order.setRemark("患者端小程序支付");
        bizPayOrderMapper.insert(order);

        WxPayChannelService.PayUnifiedResult unified = wxPayChannelService.unifiedOrder(order);
        if (unified.errMsg() != null) {
            throw new BusinessException("微信下单失败：" + unified.errMsg());
        }
        if (unified.mockPaid()) {
            // 模式：后端直接推进支付成功（真收银台模式下这一步由 /miniapp/pay/notify 异步触发）
            handlePaySuccess(order.getPayNo(), "MOCK_" + System.currentTimeMillis());
            BizPayOrder paid = getByPayNo(order.getPayNo());
            return toVO(paid, null);
        }
        // 真收银台模式：返回参数给前端 wx.requestPayment，回调异步推进
        return toVO(order, unified.payParams());
    }

    /**
     * 支付成功推进（真模式由 notify 端点调用；模式在下单事务内同步调用）。
     * 幂等：已支付直接返回。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePaySuccess(String payNo, String outTradeNo) {
        BizPayOrder order = getByPayNo(payNo);
        if (order == null) {
            throw new BusinessException("支付单不存在：" + payNo);
        }
        if (order.getPayStatus() != null && order.getPayStatus() == 1) {
            return;
        }
        order.setPayStatus(1);
        order.setOutTradeNo(outTradeNo);
        order.setPayTime(LocalDateTime.now());
        bizPayOrderMapper.updateById(order);

        switch (order.getBizType()) {
            case 1 -> advanceOutpatientCharge(order);
            case 2 -> advanceRegistCharge(order);
            case 3 -> advanceDeposit(order);
            default -> throw new BusinessException("未知业务类型：" + order.getBizType());
        }
        notifyPatient(order, "支付成功");
    }

    /**
     * 2-挂号费：给挂号账单记一笔微信收款流水（到院补缴口径的正向链）。
     *
     * <p>挂号 upsert 时收费域已出「1-挂号费结算」账单并回写 bill_id，签到硬校验账单是否付清 ——
     * 所以小程序支付成功必须落到支付资金流水，否则患者线上付了钱到院仍签不了到。
     */
    private void advanceRegistCharge(BizPayOrder order) {
        BizAppointInfo regist = bizAppointService.getById(order.getBizId());
        if (regist == null) {
            throw new BusinessException("挂号记录不存在：" + order.getBizId());
        }
        if (regist.getBillId() == null) {
            // 免收/老数据场景：没有账单就没有该收多少钱这件事，支付单仅留档，不凭空造账单
            log.info("[微信支付口子] 挂号费入账 payNo={} registId={}（无关联账单，凭证留档）",
                    order.getPayNo(), order.getBizId());
            return;
        }
        payBill(regist.getBillId(), order, "挂号费");
    }

    @Override
    public List<PayOrderListVO> myOrders() {
        var current = UserUtils.getCurrentUser();
        return bizPayOrderMapper.selectList(new LambdaQueryWrapper<BizPayOrder>()
                        .eq(BizPayOrder::getPatientId, current.getPatientId())
                        .orderByDesc(BizPayOrder::getId)
                        .last("LIMIT 50"))
                .stream().map(o -> {
                    PayOrderListVO vo = new PayOrderListVO();
                    vo.setId(String.valueOf(o.getId()));
                    vo.setPayNo(o.getPayNo());
                    vo.setBizType(o.getBizType());
                    vo.setBizId(String.valueOf(o.getBizId()));
                    vo.setAmount(o.getAmount());
                    vo.setPayStatus(o.getPayStatus());
                    vo.setPayTime(o.getPayTime());
                    return vo;
                }).toList();
    }

    @Override
    public List<PendingBillListVO> pendingBills() {
        var current = UserUtils.getCurrentUser();
        return settlementBillService.pendingBillViews(current.getPatientId()).stream().map(view -> {
            PendingBillListVO vo = new PendingBillListVO();
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
    private List<MiniappPendingBillItemVO> toPendingBillItems(List<com.his.charge.vo.PendingBillItemVO> details) {
        if (details == null) {
            return List.of();
        }
        return details.stream().map(item -> {
            MiniappPendingBillItemVO vo = new MiniappPendingBillItemVO();
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
            vo.setCatalogTypeText(catalogText(vo.getCatalogType()));
            return vo;
        }).toList();
    }

    /**
     * 医保目录类别文本。口径抄自 {@code BizSettlementBillItem.catalogType}：
     * 0-自费 1-甲类 2-乙类 3-丙类；患者端只显示，判定一律以结算结果为准。
     */
    private String catalogText(Integer catalogType) {
        return switch (catalogType == null ? -1 : catalogType) {
            case 0 -> "自费";
            case 1 -> "甲类";
            case 2 -> "乙类";
            case 3 -> "丙类";
            default -> "未分类";
        };
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundByBiz(PayRefundDTO dto) {
        BizPayOrder order = bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getBizType, dto.getBizType())
                .eq(BizPayOrder::getBizId, dto.getBizId())
                .eq(BizPayOrder::getPayStatus, 1)
                .orderByDesc(BizPayOrder::getId)
                .last("LIMIT 1"));
        if (order == null) {
            return; // 没有已支付单（例如挂号费未线上支付），无事可退
        }
        if (order.getPayStatus() == 3) {
            return; // 已退款，幂等
        }
        log.info("[微信支付口子] ===== 模拟原路退回 =====");
        log.info("[微信支付口子] 退款 payNo={} out_trade_no={} amount=￥{} reason={}（真对接时调 V3 refunds 接口）",
                order.getPayNo(), order.getOutTradeNo(), order.getAmount(),
                dto.getReason() == null ? "患者退号" : dto.getReason());
        order.setPayStatus(3);
        order.setRefundTime(LocalDateTime.now());
        order.setRemark("退款原因：" + (dto.getReason() == null ? "患者退号" : dto.getReason()));
        bizPayOrderMapper.updateById(order);
    }

    // 私有
    private BigDecimal resolveAmount(PayUpsertDTO dto) {
        // 门诊缴费：金额一律以账单剩余应缴为准（前端传值忽略，防止单据与支付金额不一致）
        if (dto.getBizType() == 1) {
            BizSettlementBill bill = settlementBillService.getById(dto.getBizId());
            if (bill == null) {
                throw new BusinessException("结算账单不存在：" + dto.getBizId());
            }
            BigDecimal unpaid = NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount()));
            if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("该账单已无应缴金额");
            }
            return unpaid.setScale(2, RoundingMode.HALF_UP);
        }
        // 挂号费/押金：以患者端传值为准
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支付金额必须大于0");
        }
        return dto.getAmount().setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 门诊缴费推进：给账单记一笔微信收款流水（与院内收费台同一入口，只是流水来源写「患者端支付」）
     */
    private void advanceOutpatientCharge(BizPayOrder order) {
        payBill(order.getBizId(), order, "门诊缴费");
    }

    /**
     * 支付成功 → 账单收款（L3 一笔流水）。
     *
     * <p>金额取<b>账单剩余应缴</b>而不是支付单上的 amount：患者端页面停留期间窗口可能已经收过一笔，
     * 按页面金额收就是多收，多收的那部分是要再走退费流程的真钱。
     * 已无应缴时只留档不报错 —— 钱是渠道已收的事实，但本院账上不缺这笔，重复收款交给日结勾对。
     */
    private void payBill(Long billId, BizPayOrder order, String scene) {
        BizSettlementBill bill = settlementBillService.getById(billId);
        if (bill == null) {
            throw new BusinessException(scene + "推进失败：结算账单不存在（" + billId + "）");
        }
        BigDecimal unpaid = NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount()));
        if (unpaid.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[微信支付口子] {} 账单 {} 已无应缴（可能窗口已收），支付单 {} 仅留档 payNo={}",
                    scene, billId, order.getPayNo(), order.getPayNo());
            return;
        }
        BillPayDTO dto = new BillPayDTO();
        dto.setBillId(billId);
        dto.setSourceType(TxnSourceEnum.MINIAPP.getCode());
        BillPayDTO.PayItem item = new BillPayDTO.PayItem();
        item.setPayMethod(PaymentMethodEnum.WECHAT.getCode());
        item.setAmount(unpaid);
        item.setChannelTxnNo(order.getOutTradeNo());
        item.setRemark(scene + "：患者端微信支付 支付单 " + order.getPayNo());
        dto.setItems(List.of(item));
        paymentService.pay(dto);
        log.info("[微信支付口子] {}已入账账单 payNo={} billId={} amount=￥{}",
                scene, order.getPayNo(), billId, unpaid.toPlainString());
    }

    /**
     * 押金推进：走住院预交金充值入口（payMethod=2 微信），余额与院内同一套流水、同一个账户。
     *
     * <p>渠道交易号必须带上：日后原路退回要靠它向微信发起退款，不带就只能退现金，
     * 收银台当天抽屉里会多出一笔说不清的现金。
     */
    private void advanceDeposit(BizPayOrder order) {
        PrepayUpsertDTO prepay = new PrepayUpsertDTO();
        prepay.setAdmissionId(order.getBizId());
        prepay.setPrepayType(1);
        prepay.setAmount(order.getAmount());
        prepay.setPayMethod(2);
        prepay.setChannelTxnNo(order.getOutTradeNo());
        prepay.setPayTime(order.getPayTime());
        prepay.setRemark("小程序微信充值（支付单 " + order.getPayNo() + "）");
        inpatientAccountService.savePrepay(prepay);
        log.info("[微信支付口子] 住院押金入账 payNo={} admissionId={} amount=￥{}",
                order.getPayNo(), order.getBizId(), order.getAmount());
    }

    /**
     * 支付回执通知（微信订阅消息口子；通道未启用时实现内部留痕，不外抛）
     */
    private void notifyPatient(BizPayOrder order, String title) {
        try {
            var current = UserUtils.getCurrentUser();
            if (current == null || current.getUserId() == null) {
                return;
            }
            sysMessageService.sendWechatMessage(current.getUserId(), current.getRealName(), "pay_receipt",
                    "pages/payment/payment",
                    Map.of("amount", order.getAmount().toPlainString() + "元", "payNo", order.getPayNo()),
                    title, "您有一笔 " + order.getAmount().toPlainString() + " 元支付已成功（" + order.getPayNo() + "）",
                    "pay", order.getId());
        } catch (Exception e) {
            log.warn("支付回执通知失败 payNo={} err={}", order.getPayNo(), e.getMessage());
        }
    }

    private BizPayOrder getByPayNo(String payNo) {
        return bizPayOrderMapper.selectOne(new LambdaQueryWrapper<BizPayOrder>()
                .eq(BizPayOrder::getPayNo, payNo)
                .last("LIMIT 1"));
    }

    private PayOrderVO toVO(BizPayOrder order, Map<String, String> payParams) {
        PayOrderVO vo = new PayOrderVO();
        vo.setPayNo(order.getPayNo());
        vo.setPayOrderId(order.getId());
        vo.setPayStatus(order.getPayStatus());
        vo.setAmount(order.getAmount());
        vo.setPayParams(payParams);
        return vo;
    }
}
