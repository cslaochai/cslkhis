package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.BillPayDTO;
import com.his.charge.dto.BillRefundDTO;
import com.his.charge.dto.BillVoidDTO;
import com.his.charge.dto.PaymentTxnQueryPageDTO;
import com.his.charge.entity.*;
import com.his.charge.mapper.BizFundAccountTxnMapper;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.service.*;
import com.his.charge.vo.BizPaymentTxnVO;
import com.his.common.base.PageResult;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付资金流水实现（L3）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<BizPaymentTxnMapper, BizPaymentTxn> implements PaymentService {

    private static final int AMOUNT_SCALE = 2;

    /**
     * 无员工上下文（患者端自助、系统任务）时的收银人占位ID：班结会把它单列一栏，不与真人混在一起
     */
    private static final long CASHIER_SYSTEM = 0L;

    private static final int W_TXN_NO = 32;
    private static final int W_BILL_NO = 32;
    private static final int W_PATIENT_NO = 32;
    private static final int W_PATIENT_NAME = 50;
    private static final int W_CHANNEL_TXN_NO = 64;
    private static final int W_CASHIER_NAME = 50;
    private static final int W_REASON = 500;
    private static final int W_APPLY_NO = 32;
    private static final int W_RECEIPT_NO = 32;
    private static final int W_REMARK = 500;

    private final SettlementBillService settlementBillService;
    private final FeeRecordService feeRecordService;
    private final SourceAdvanceService sourceAdvanceService;
    private final FundAccountService fundAccountService;
    private final BizFundAccountTxnMapper fundAccountTxnMapper;
    private final BizSettlementBillItemMapper billItemMapper;
    private final PayRefundService payRefundService;
    private final RedisSequenceService redisSequenceService;
    private final InsuranceSettlementService insuranceSettlementService;

    private static List<BizPaymentTxnVO> toVOList(List<BizPaymentTxn> txns) {
        List<BizPaymentTxnVO> records = new ArrayList<>(txns.size());
        for (BizPaymentTxn txn : txns) {
            BizPaymentTxnVO vo = new BizPaymentTxnVO();
            BeanUtils.copyProperties(txn, vo);
            records.add(vo);
        }
        return records;
    }

    private static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizPaymentTxnVO> pay(BillPayDTO dto) {
        if (dto == null || dto.getBillId() == null || CollectionUtils.isEmpty(dto.getItems())) {
            throw new BusinessException("缺少账单或收款明细");
        }
        BizSettlementBill bill = requirePayableBill(dto.getBillId());
        TxnSourceEnum source = TxnSourceEnum.fromCode(dto.getSourceType());
        if (source == null) {
            source = TxnSourceEnum.CASHIER;
        }
        if (source.refundKind()) {
            throw new BusinessException("收款流水的来源不能是退费类");
        }

        BigDecimal remaining = scale(bill.getPayableAmount()
                .subtract(nz(bill.getPaidAmount())).add(nz(bill.getRefundAmount())));
        BigDecimal total = BigDecimal.ZERO;
        for (BillPayDTO.PayItem item : dto.getItems()) {
            total = total.add(nz(item.getAmount()));
        }
        total = scale(total);
        if (total.compareTo(remaining) > 0) {
            throw new BusinessException("收款合计 " + total.toPlainString() + " 超过尚需缴纳的 " + remaining.toPlainString());
        }

        List<BizPaymentTxn> txns = new ArrayList<>();
        for (BillPayDTO.PayItem item : dto.getItems()) {
            txns.add(chargeOne(bill, item, source));
        }
        settlementBillService.refreshFromTxns(bill.getId());
        log.info("[收款] 账单 {} 收 {} 笔 合计 ¥{}（尚需 ¥{}）来源={}", bill.getBillNo(), txns.size(),
                total.toPlainString(), remaining.toPlainString(), source.getDesc());
        return toVOList(txns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizPaymentTxnVO> refund(BillRefundDTO dto) {
        if (dto == null || dto.getBillId() == null) {
            throw new BusinessException("缺少账单");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("缺少退费原因");
        }
        BizSettlementBill bill = settlementBillService.getById(dto.getBillId());
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())) {
            throw new BusinessException("账单已作废，没有可退的收款");
        }
        TxnSourceEnum source = TxnSourceEnum.fromCode(dto.getSourceType());
        if (source == null) {
            source = TxnSourceEnum.DIRECT_REFUND;
        }
        if (!source.refundKind()) {
            throw new BusinessException("退费流水的来源必须是退费类（退费申请执行/收费处直退/退号联动/出院结算退差）");
        }

        List<BizSettlementBillItem> items = billItemMapper.selectByBill(bill.getId());
        List<Long> feeIds = resolveRefundFeeIds(items, dto.getFeeIds());
        // 没点名记账行 = 整单全退；点了名但要退的行数正好等于账单全部行，也算整单
        boolean wholeBill = CollectionUtils.isEmpty(dto.getFeeIds()) || feeIds.size() >= items.size();
        // 医保门禁必须放在动钱之前：清单已报盘时医保侧只有整单冲正（2305），部分退要当场拒绝
        insuranceSettlementService.assertBillRefundable(bill.getId(), wholeBill);
        // 药品闸门同样在动钱之前：药已发到患者手里、钱先退了，退费流程不会替药师把药收回架
        if (!CollectionUtils.isEmpty(feeIds)) {
            sourceAdvanceService.assertDrugReturnedForRefund(feeRecordService.listByIds(feeIds), "退费");
        }

        Map<Long, BizSettlementBillItem> itemByFee = itemByFeeId(items);
        BigDecimal refundTotal = BigDecimal.ZERO;
        BigDecimal paidPart = BigDecimal.ZERO;
        for (Long feeId : feeIds) {
            BizFeeRecord neg = feeRecordService.reverseForRefund(feeId, dto.getReason());
            BigDecimal gross = nz(neg.getAmount()).abs();
            refundTotal = refundTotal.add(gross);
            paidPart = paidPart.add(patientShare(itemByFee.get(feeId), gross));
        }
        refundTotal = scale(refundTotal);
        paidPart = scale(paidPart);
        if (refundTotal.signum() == 0) {
            throw new BusinessException("本次退费金额为 0，不生成退费流水");
        }
        BigDecimal charged = scale(nz(bill.getPaidAmount()).subtract(nz(bill.getRefundAmount())));
        if (paidPart.compareTo(charged) > 0) {
            // 比的是「患者掏的那部分」而不是记账行全额：医保账单里统筹那一段从来没收进柜面，
            // 拿它去比净已收，任何一张有统筹记账的账单都退不掉（关键口径，别改回 refundTotal）
            throw new BusinessException("退费金额 " + refundTotal.toPlainString() + "（其中患者实付 "
                    + paidPart.toPlainString() + "）超过该账单净已收 " + charged.toPlainString());
        }

        List<BizPaymentTxn> refunds = payBackEachOrigTxn(bill, paidPart, source, dto);
        settlementBillService.refreshFromTxns(bill.getId());
        // 钱退回去了，来源单据必须跟着退回未缴费：处方挂着「已缴费」，药房就会把退掉的药发出去
        sourceAdvanceService.revertByBill(bill.getId(), dto.getReason());
        // 账单的钱变了，医保清单必须跟着动：整单退 → 作废（已报盘的连带发 2305 撤回）；
        // 部分退 → 三个数不再成立，退回待结算等钱收齐后重新结算
        if (wholeBill) {
            insuranceSettlementService.voidByBill(bill.getId(), dto.getReason());
        } else {
            insuranceSettlementService.resetByBill(bill.getId(), dto.getReason());
        }
        log.info("[退费] 账单 {} 红冲记账行 {} 条，冲减费用 ¥{}（患者实付 ¥{}）分 {} 笔原路退回，来源={} 原因：{}",
                bill.getBillNo(), feeIds.size(), refundTotal.toPlainString(), paidPart.toPlainString(),
                refunds.size(), source.getDesc(), dto.getReason());
        return toVOList(refunds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeBill(Long billId, String reason, Integer sourceType) {
        BizSettlementBill bill = settlementBillService.getById(billId);
        if (bill == null) {
            return false;
        }
        // 作废/已退平的账单不再处理：撤销是幂等的，退号连点两下不能刷出第二笔退款
        if (BillStatusEnum.VOIDED.getCode().equals(bill.getBillStatus())
                || BillStatusEnum.REFUNDED.getCode().equals(bill.getBillStatus())) {
            return false;
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("缺少撤销原因");
        }
        TxnSourceEnum source = TxnSourceEnum.fromCode(sourceType);
        if (source == null || !source.refundKind()) {
            source = TxnSourceEnum.DIRECT_REFUND;
        }

        // 记账行清单必须在撤销前拿：账单一作废就清空 bill_id，那时再也认不出这些行原本属于它
        List<BizFeeRecord> rows = feeRecordService.listByBill(billId);
        // 药品闸门：撤销也是退钱，药已发未退同样不能撤（挂号单没有处方锚点，天然放行）
        sourceAdvanceService.assertDrugReturnedForRefund(rows, "撤销账单");
        BigDecimal net = scale(nz(bill.getPaidAmount()).subtract(nz(bill.getRefundAmount())));

        BillRefundDTO refundDTO = new BillRefundDTO();
        refundDTO.setBillId(billId);
        refundDTO.setReason(reason);
        refundDTO.setSourceType(source.getCode());
        // 退的是「净已收」而不是「应收合计」：欠 100 只收了 40 的账单，渠道里只有 40 可退
        List<BizPaymentTxn> refunds = net.signum() > 0
                ? payBackEachOrigTxn(bill, net, source, refundDTO) : new ArrayList<>();

        // 账单镜像必须先跟着流水走，否则 voidBill 一看「还有净已收」就把作废挡回来
        settlementBillService.refreshFromTxns(billId);

        BillVoidDTO voidDTO = new BillVoidDTO();
        voidDTO.setBillId(billId);
        voidDTO.setReason(reason);
        settlementBillService.voidBill(voidDTO);

        int reversed = 0;
        List<Long> reversedFeeIds = new ArrayList<>();
        for (BizFeeRecord row : rows) {
            if (nz(row.getAmount()).signum() > 0 && !FeeStatusEnum.REVERSED.getCode().equals(row.getFeeStatus())) {
                feeRecordService.reverse(row.getId(), reason);
                reversedFeeIds.add(row.getId());
                reversed++;
            }
        }
        // 账单一作废就把 bill_id 清空了，只能按 ID 把这批行重捞一次（拿到的是红冲后的最新状态），
        // 让来源单据跟着退回未缴费 —— 否则整单退掉的处方还挂着「已缴费」，药房照发不误
        if (!reversedFeeIds.isEmpty()) {
            sourceAdvanceService.revertByRows(feeRecordService.listByIds(reversedFeeIds), reason);
        }
        log.info("[整单撤销] 账单 {} 退款 ¥{} 分 {} 笔，红冲记账行 {} 条，来源={} 原因：{}",
                bill.getBillNo(), net.toPlainString(), refunds.size(), reversed, source.getDesc(), reason);
        return true;
    }

    // 住院预交金：bill_id 为空的收/退流水 + 住院资金账户（sql/140）

    @Override
    public PageResult<BizPaymentTxnVO> selectPage(PaymentTxnQueryPageDTO query) {
        Page<BizPaymentTxn> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), buildWrapper(query));
        List<BizPaymentTxnVO> records = new ArrayList<>();
        for (BizPaymentTxn txn : page.getRecords()) {
            BizPaymentTxnVO vo = new BizPaymentTxnVO();
            BeanUtils.copyProperties(txn, vo);
            records.add(vo);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<BizPaymentTxn> listByBill(Long billId) {
        return baseMapper.selectByBill(billId);
    }

    @Override
    public List<BizPaymentTxnVO> listByBillVO(Long billId) {
        return toVOList(baseMapper.selectByBill(billId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int claimForShift(Long cashierId, LocalDateTime periodEnd, Long settlementId) {
        if (cashierId == null || periodEnd == null || settlementId == null) {
            throw new BusinessException("交班归集缺少参数");
        }
        BizPaymentTxn patch = new BizPaymentTxn();
        patch.setCashierSettlementId(settlementId);
        // 只挑还没被认领的：重复交班不会把上一个班认领的流水抢过来，
        // 也让本方法天然幂等 —— 归集是登记，不是重新划分。
        // 不设下界（见接口注释）：这一秒之后才到达的收款若不归下次交班，就永久没人认领。
        return baseMapper.update(patch, new LambdaUpdateWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getCashierId, cashierId)
                .eq(BizPaymentTxn::getTxnStatus, PayTxnStatusEnum.SUCCESS.getCode())
                .isNull(BizPaymentTxn::getCashierSettlementId)
                .le(BizPaymentTxn::getTxnTime, periodEnd));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizPaymentTxn prepayDeposit(PrepaySpec spec) {
        BigDecimal amount = requirePrepayAmount(spec);
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(spec.payMethod());
        if (payMethod == null) {
            throw new BusinessException("支付方式不合法");
        }
        if (payMethod == PaymentMethodEnum.BALANCE || payMethod == PaymentMethodEnum.INSURANCE_ACCOUNT) {
            // 拿院内余额去"充"预交金，钱没进过现金抽屉却在流水里成了收入，日结必然多出一块；
            // 医保个账是刷参保人卡扣的额度，它该出现在账单的自付侧，不是预交金来源
            throw new BusinessException("预交金充值只能用现金/微信/支付宝/银行卡/转账，不能用" + payMethod.getDesc());
        }
        LocalDateTime txnTime = spec.txnTime() == null ? LocalDateTime.now() : spec.txnTime();
        BizPaymentTxn txn = new BizPaymentTxn();
        txn.setTxnNo(cut(redisSequenceService.generatePayTxnNo(), W_TXN_NO));
        // bill_id / bill_no 留空：这笔钱没有对应账单（为什么不为它造一张 0 元账单，见接口注释）
        txn.setPatientId(spec.patientId());
        txn.setPatientNo(cut(spec.patientNo(), W_PATIENT_NO));
        txn.setPatientName(cut(spec.patientName(), W_PATIENT_NAME));
        txn.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        txn.setEncounterId(spec.admissionId());
        txn.setDirection(PayDirectionEnum.CHARGE.getCode());
        txn.setPayMethod(payMethod.getCode());
        txn.setAmount(amount);
        txn.setTxnStatus(PayTxnStatusEnum.SUCCESS.getCode());
        txn.setSourceType(TxnSourceEnum.PREPAY.getCode());
        txn.setChannelTxnNo(cut(channelNoOf(payMethod, null, spec.channelTxnNo(), txn), W_CHANNEL_TXN_NO));
        txn.setCashierId(currentCashier());
        txn.setCashierName(cut(UserUtils.getCurrentUser().getRealName(), W_CASHIER_NAME));
        txn.setTxnTime(txnTime);
        txn.setTxnDate(txnTime.toLocalDate());
        txn.setReceiptNo(cut(spec.receiptNo(), W_RECEIPT_NO));
        txn.setRemark(cut(spec.remark(), W_REMARK));
        this.save(txn);

        fundAccountService.apply(new FundAccountService.FundTxnSpec(
                AccountOwnerTypeEnum.ADMISSION.getCode(), spec.admissionId(),
                spec.patientId(), spec.patientNo(), spec.patientName(),
                AccountTxnTypeEnum.PREPAY_RECHARGE.getCode(), amount,
                spec.admissionId(), null, txn.getId(),
                payMethod.getCode(), txn.getTxnNo(), "住院预交金充值 " + txn.getTxnNo() + "（" + payMethod.getDesc() + "）"));
        log.info("[预交金收款] 入院 {} 流水 {} ¥{} 方式={} 收据={} 操作人={}", spec.admissionId(), txn.getTxnNo(),
                amount.toPlainString(), payMethod.getDesc(), spec.receiptNo(), txn.getCashierName());
        return txn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizPaymentTxn> prepayRefund(PrepaySpec spec) {
        BigDecimal amount = requirePrepayAmount(spec);
        BigDecimal balance = scale(nz(fundAccountService.admissionBalance(spec.admissionId())));
        if (amount.compareTo(balance) > 0) {
            throw new BusinessException("退款金额 " + amount.toPlainString() + " 超过当前预交金余额 "
                    + balance.toPlainString() + "，不能退款");
        }
        return refundPrepay(spec, amount, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizPaymentTxn> dischargeRemainder(PrepaySpec spec) {
        BigDecimal amount = requirePrepayAmount(spec);
        BigDecimal balance = scale(nz(fundAccountService.admissionBalance(spec.admissionId())));
        if (amount.compareTo(balance) > 0) {
            throw new BusinessException("出院退差 " + amount.toPlainString() + " 超过住院账户余额 "
                    + balance.toPlainString() + "，请先核对本次结算");
        }
        return refundPrepay(spec, amount, true);
    }

    /**
     * 按原充值流水 FIFO 逐笔退回：一笔充值退一笔钱，退超了渠道侧就成了长款。
     */
    private List<BizPaymentTxn> refundPrepay(PrepaySpec spec, BigDecimal amount, boolean toPatientWallet) {
        BigDecimal left = amount;
        List<BizPaymentTxn> refunds = new ArrayList<>();
        for (BizPaymentTxn orig : baseMapper.selectPrepayDeposits(spec.admissionId())) {
            if (left.signum() <= 0) {
                break;
            }
            BigDecimal refundable = scale(nz(baseMapper.sumRefundableByTxn(orig.getId())));
            if (refundable.signum() <= 0) {
                continue;
            }
            BigDecimal part = refundable.compareTo(left) >= 0 ? left : refundable;
            refunds.add(refundPrepayOne(spec, orig, part, toPatientWallet));
            left = scale(left.subtract(part));
        }
        if (left.signum() > 0) {
            // 账户说还有这么多钱，可退的充值流水却摊不完 —— 两边已经不一致，宁可拒绝也不许硬退
            throw new BusinessException("可退的预交金充值流水不足，尚差 " + left.toPlainString() + "，请先核对支付流水");
        }
        return refunds;
    }

    private BizPaymentTxn refundPrepayOne(PrepaySpec spec, BizPaymentTxn orig, BigDecimal amount, boolean toPatientWallet) {
        LocalDateTime txnTime = spec.txnTime() == null ? LocalDateTime.now() : spec.txnTime();
        String txnNo = cut(redisSequenceService.generateRefundTxnNo(), W_TXN_NO);
        Integer origMethod = orig.getPayMethod();
        BizFundAccountTxn walletTxn = null;
        String channelRefundNo;
        if (toPatientWallet) {
            // 钱没离开医院，只是从这次住院的预交金搬到患者的院内余额账户
            walletTxn = fundAccountService.apply(new FundAccountService.FundTxnSpec(
                    AccountOwnerTypeEnum.PATIENT.getCode(), spec.patientId(),
                    spec.patientId(), spec.patientNo(), spec.patientName(),
                    AccountTxnTypeEnum.DISCHARGE_DIFF_IN.getCode(), amount,
                    null, null, null, PaymentMethodEnum.BALANCE.getCode(), txnNo,
                    "出院结算退差入账（入院 " + spec.admissionId() + "）"));
            channelRefundNo = walletTxn.getTxnNo();
        } else if (RefundMethodEnum.viaChannel(origMethod)) {
            // 渠道请求放在落库之前：失败就整笔回滚，不允许"台账冲了、钱没退出去"
            PayRefundService.RefundReceipt receipt = payRefundService.refund(new PayRefundService.RefundRequest(
                    origMethod, orig.getTxnNo(), txnNo, amount, spec.remark()));
            if (receipt == null || !receipt.success()) {
                String err = receipt == null ? "渠道无响应" : receipt.errMsg();
                throw new BusinessException("渠道原路退回失败，本次预交金退款已撤销：" + cut(err, 200));
            }
            channelRefundNo = receipt.channelRefundNo();
        } else {
            // 现金由柜面点钞出去、转账人工退回，都不产生渠道号
            channelRefundNo = null;
        }

        Integer outMethod = toPatientWallet ? PaymentMethodEnum.BALANCE.getCode() : origMethod;
        BizPaymentTxn txn = new BizPaymentTxn();
        txn.setTxnNo(txnNo);
        txn.setPatientId(spec.patientId());
        txn.setPatientNo(cut(spec.patientNo(), W_PATIENT_NO));
        txn.setPatientName(cut(spec.patientName(), W_PATIENT_NAME));
        txn.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        txn.setEncounterId(spec.admissionId());
        txn.setDirection(PayDirectionEnum.REFUND.getCode());
        txn.setPayMethod(outMethod);
        txn.setAmount(amount.negate());
        txn.setTxnStatus(PayTxnStatusEnum.SUCCESS.getCode());
        txn.setOrigTxnId(orig.getId());
        // 退差与柜面退现分码值：前者钱没离开医院（不进点钞），后者离开了。
        // 混成 source_type=3 会让"柜面已退预交金"合计凭空多出一笔抽屉里没付过的钱
        txn.setSourceType(toPatientWallet ? TxnSourceEnum.DISCHARGE_DIFF.getCode() : TxnSourceEnum.PREPAY.getCode());
        txn.setRefundMethod(toPatientWallet ? RefundMethodEnum.BALANCE.getCode()
                : RefundMethodEnum.ofPayMethod(origMethod).getCode());
        txn.setChannelTxnNo(cut(channelRefundNo, W_CHANNEL_TXN_NO));
        txn.setCashierId(currentCashier());
        txn.setCashierName(cut(UserUtils.getCurrentUser().getRealName(), W_CASHIER_NAME));
        txn.setTxnTime(txnTime);
        txn.setTxnDate(txnTime.toLocalDate());
        txn.setRemark(cut(spec.remark(), W_REMARK));
        this.save(txn);

        fundAccountService.apply(new FundAccountService.FundTxnSpec(
                AccountOwnerTypeEnum.ADMISSION.getCode(), spec.admissionId(),
                spec.patientId(), spec.patientNo(), spec.patientName(),
                AccountTxnTypeEnum.PREPAY_REFUND.getCode(), amount,
                spec.admissionId(), null, txn.getId(), outMethod, txnNo,
                (toPatientWallet ? "出院结算退差（" : "预交金退款 ") + txn.getTxnNo() + "）"));
        if (walletTxn != null) {
            // 两条账户流水指同一条支付流水：住院侧扣、患者侧入，缺任何一条这笔钱就凭空消失或多出来
            walletTxn.setPaymentTxnId(txn.getId());
            fundAccountTxnMapper.updateById(walletTxn);
        }
        log.info("[预交金退款] 入院 {} 流水 {} ¥{} 原流水={} 去向={} 操作人={}", spec.admissionId(), txn.getTxnNo(),
                amount.toPlainString(), orig.getTxnNo(), toPatientWallet ? "患者院内余额" : "原路退回", txn.getCashierName());
        return txn;
    }

    /**
     * 预交金入参底线：主体齐全 + 金额为正。
     *
     * <p>方向由调的方法决定，所以这里必须挡住负数 —— 传 -500 的"退款"在 FIFO 摊派下会退出不存在的钱。
     */
    private BigDecimal requirePrepayAmount(PrepaySpec spec) {
        if (spec == null || spec.admissionId() == null || spec.patientId() == null) {
            throw new BusinessException("预交金缺少住院账户主体（入院ID/患者ID）");
        }
        if (spec.amount() == null) {
            throw new BusinessException("缺少变动金额");
        }
        BigDecimal amount = scale(spec.amount());
        if (amount.signum() <= 0) {
            throw new BusinessException("预交金金额必须大于 0，方向由接口决定（退款也传正数）");
        }
        return amount;
    }

    /**
     * 一笔收款 = 一行流水。渠道类先定好流水号，渠道号由它派生（对账时两边勾得上）。
     */
    private BizPaymentTxn chargeOne(BizSettlementBill bill, BillPayDTO.PayItem item, TxnSourceEnum source) {
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(item.getPayMethod());
        if (payMethod == null) {
            throw new BusinessException("支付方式不合法");
        }
        BigDecimal amount = scale(item.getAmount());
        if (amount.signum() <= 0) {
            throw new BusinessException("收款金额必须大于 0");
        }
        String txnNo = cut(redisSequenceService.generatePayTxnNo(), W_TXN_NO);
        BizFundAccountTxn accountTxn = null;
        if (payMethod == PaymentMethodEnum.BALANCE) {
            accountTxn = deductBalance(bill, item, amount, txnNo);
        }

        BizPaymentTxn txn = new BizPaymentTxn();
        txn.setTxnNo(txnNo);
        txn.setBillId(bill.getId());
        txn.setBillNo(cut(bill.getBillNo(), W_BILL_NO));
        txn.setPatientId(bill.getPatientId());
        txn.setPatientNo(cut(bill.getPatientNo(), W_PATIENT_NO));
        txn.setPatientName(cut(bill.getPatientName(), W_PATIENT_NAME));
        txn.setEncounterType(bill.getEncounterType());
        txn.setEncounterId(bill.getEncounterId());
        txn.setDirection(PayDirectionEnum.CHARGE.getCode());
        txn.setPayMethod(payMethod.getCode());
        txn.setAmount(amount);
        txn.setTxnStatus(PayTxnStatusEnum.SUCCESS.getCode());
        txn.setSourceType(source.getCode());
        txn.setChannelTxnNo(cut(channelNoOf(payMethod, accountTxn, item.getChannelTxnNo(), txn), W_CHANNEL_TXN_NO));
        txn.setCashierId(currentCashier());
        txn.setCashierName(cut(UserUtils.getCurrentUser().getRealName(), W_CASHIER_NAME));
        txn.setTxnTime(LocalDateTime.now());
        txn.setTxnDate(LocalDate.now());
        txn.setReason(null);
        txn.setRemark(cut(item.getRemark(), W_REMARK));
        this.save(txn);
        if (accountTxn != null) {
            // 账户流水与支付流水互相指认：缺任何一条都是"钱动了账没动"，同事务里补上指针
            accountTxn.setPaymentTxnId(txn.getId());
            fundAccountTxnMapper.updateById(accountTxn);
        }
        return txn;
    }

    /**
     * 余额支付：先扣账户（余额不足当场拒绝），再落收款流水。
     *
     * <p>门诊余额挂在<b>患者</b>名下、住院预交金挂在<b>入院</b>名下，所以账户主体跟着账单走。
     */
    private BizFundAccountTxn deductBalance(BizSettlementBill bill, BillPayDTO.PayItem item,
                                            BigDecimal amount, String payTxnNo) {
        boolean inpatient = EncounterTypeEnum.INPATIENT.getCode().equals(bill.getEncounterType());
        Integer ownerType = inpatient ? AccountOwnerTypeEnum.ADMISSION.getCode() : AccountOwnerTypeEnum.PATIENT.getCode();
        Long ownerId = item.getOwnerId() != null ? item.getOwnerId()
                : (inpatient ? bill.getEncounterId() : bill.getPatientId());
        if (ownerId == null) {
            throw new BusinessException("余额支付缺少账户主体");
        }
        return fundAccountService.apply(new FundAccountService.FundTxnSpec(
                ownerType, ownerId, bill.getPatientId(), bill.getPatientNo(), bill.getPatientName(),
                AccountTxnTypeEnum.BALANCE_PAY.getCode(), amount,
                inpatient ? bill.getEncounterId() : null, bill.getId(), null,
                PaymentMethodEnum.BALANCE.getCode(), payTxnNo, "账单 " + bill.getBillNo() + " 余额支付"));
    }

    /**
     * 渠道号口径：账户流水号（余额）＞ 调用方带回的真实交易号（患者端支付回调）＞ 柜面扫码的模拟号。
     *
     * <p>模拟号与 {@code PayChannelService} 生成渠道账单用的是同一套拼法，
     * 所以"渠道账单 ↔ 本地流水"的勾对链路能真命中；真渠道接入后走第二种，本方法不用改。
     */
    private String channelNoOf(PaymentMethodEnum payMethod, BizFundAccountTxn accountTxn,
                               String provided, BizPaymentTxn txn) {
        if (accountTxn != null) {
            return accountTxn.getTxnNo();
        }
        if (StringUtils.hasText(provided)) {
            return provided;
        }
        if (!payMethod.channelBacked()) {
            return null;
        }
        return "SIMU-" + payMethod.getCode() + "-" + LocalDate.now().format(DateFormats.COMPACT_DATE) + "-" + txn.getTxnNo();
    }

    /**
     * 按原收款流水逐笔退回：FIFO 吃掉每笔收款的可退余额，直到本次退费金额摊完。
     */
    private List<BizPaymentTxn> payBackEachOrigTxn(BizSettlementBill bill, BigDecimal refundTotal,
                                                   TxnSourceEnum source, BillRefundDTO dto) {
        BigDecimal left = refundTotal;
        List<BizPaymentTxn> refunds = new ArrayList<>();
        for (BizPaymentTxn orig : baseMapper.selectByBill(bill.getId())) {
            if (left.signum() <= 0) {
                break;
            }
            if (!PayDirectionEnum.CHARGE.getCode().equals(orig.getDirection())
                    || !PayTxnStatusEnum.SUCCESS.getCode().equals(orig.getTxnStatus())) {
                continue;
            }
            BigDecimal refundable = scale(nz(baseMapper.sumRefundableByTxn(orig.getId())));
            if (refundable.signum() <= 0) {
                continue;
            }
            BigDecimal part = refundable.compareTo(left) >= 0 ? left : refundable;
            refunds.add(refundOne(bill, orig, part, source, dto));
            left = scale(left.subtract(part));
        }
        if (left.signum() > 0) {
            // 账单镜像说收过这么多钱，流水里却退不出来 —— 说明两边已经不一致，宁可拒绝也不许硬退
            throw new BusinessException("可退的收款流水不足，尚差 " + left.toPlainString() + "，请先核对支付流水");
        }
        return refunds;
    }

    private BizPaymentTxn refundOne(BizSettlementBill bill, BizPaymentTxn orig, BigDecimal amount,
                                    TxnSourceEnum source, BillRefundDTO dto) {
        PaymentMethodEnum payMethod = PaymentMethodEnum.getByCode(orig.getPayMethod());
        if (payMethod == null) {
            throw new BusinessException("原收款流水的支付方式无法识别，不能退费");
        }
        String txnNo = cut(redisSequenceService.generateRefundTxnNo(), W_TXN_NO);
        RefundMethodEnum refundMethod = RefundMethodEnum.ofPayMethod(orig.getPayMethod());

        // 渠道请求放在落库之前：失败就整笔回滚，不允许出现"台账冲了、钱没退出去"
        String channelRefundNo = null;
        BizFundAccountTxn accountTxn = null;
        if (RefundMethodEnum.viaChannel(orig.getPayMethod())) {
            PayRefundService.RefundReceipt receipt = payRefundService.refund(new PayRefundService.RefundRequest(
                    orig.getPayMethod(), bill.getBillNo(), txnNo, amount, dto.getReason()));
            if (receipt == null || !receipt.success()) {
                String err = receipt == null ? "渠道无响应" : receipt.errMsg();
                throw new BusinessException("渠道原路退回失败，本次退费已撤销：" + cut(err, 200));
            }
            channelRefundNo = receipt.channelRefundNo();
        } else if (payMethod == PaymentMethodEnum.BALANCE) {
            // 余额付的钱退回患者账户（不是柜面点钞）：账户必须同一时刻收到这笔入账，否则患者余额对不上
            accountTxn = refundBalance(bill, amount, txnNo);
            channelRefundNo = accountTxn.getTxnNo();
        }

        BizPaymentTxn txn = new BizPaymentTxn();
        txn.setTxnNo(txnNo);
        txn.setBillId(bill.getId());
        txn.setBillNo(cut(bill.getBillNo(), W_BILL_NO));
        txn.setPatientId(bill.getPatientId());
        txn.setPatientNo(cut(bill.getPatientNo(), W_PATIENT_NO));
        txn.setPatientName(cut(bill.getPatientName(), W_PATIENT_NAME));
        txn.setEncounterType(bill.getEncounterType());
        txn.setEncounterId(bill.getEncounterId());
        txn.setDirection(PayDirectionEnum.REFUND.getCode());
        txn.setPayMethod(orig.getPayMethod());
        txn.setAmount(amount.negate());
        txn.setTxnStatus(PayTxnStatusEnum.SUCCESS.getCode());
        txn.setOrigTxnId(orig.getId());
        txn.setSourceType(source.getCode());
        txn.setRefundMethod(refundMethod.getCode());
        txn.setChannelTxnNo(cut(channelRefundNo, W_CHANNEL_TXN_NO));
        txn.setCashierId(currentCashier());
        txn.setCashierName(cut(UserUtils.getCurrentUser().getRealName(), W_CASHIER_NAME));
        txn.setTxnTime(LocalDateTime.now());
        txn.setTxnDate(LocalDate.now());
        txn.setReason(cut(dto.getReason(), W_REASON));
        txn.setApplyId(dto.getApplyId());
        txn.setApplyNo(cut(dto.getApplyNo(), W_APPLY_NO));
        this.save(txn);
        if (accountTxn != null) {
            accountTxn.setPaymentTxnId(txn.getId());
            fundAccountTxnMapper.updateById(accountTxn);
        }
        return txn;
    }

    /**
     * 退费回补账户余额：门诊挂患者、住院挂入院，与扣用时同一个主体。
     */
    private BizFundAccountTxn refundBalance(BizSettlementBill bill, BigDecimal amount, String payTxnNo) {
        boolean inpatient = EncounterTypeEnum.INPATIENT.getCode().equals(bill.getEncounterType());
        return fundAccountService.apply(new FundAccountService.FundTxnSpec(
                inpatient ? AccountOwnerTypeEnum.ADMISSION.getCode() : AccountOwnerTypeEnum.PATIENT.getCode(),
                inpatient ? bill.getEncounterId() : bill.getPatientId(),
                bill.getPatientId(), bill.getPatientNo(), bill.getPatientName(),
                AccountTxnTypeEnum.BALANCE_REFUND.getCode(), amount,
                inpatient ? bill.getEncounterId() : null, bill.getId(), null,
                PaymentMethodEnum.BALANCE.getCode(), payTxnNo, "账单 " + bill.getBillNo() + " 退费回补余额"));
    }

    /**
     * 本次要冲减哪些记账行：给了ID按ID取（必须属于本账单），没给则整单全退。
     *
     * <p>账单行由调用方查一次传进来：判断「是不是整单退」要用同一份行数，查两次就可能得出两个答案。
     */
    private Map<Long, BizSettlementBillItem> itemByFeeId(List<BizSettlementBillItem> items) {
        Map<Long, BizSettlementBillItem> map = new HashMap<>();
        for (BizSettlementBillItem item : items) {
            map.put(item.getFeeRecordId(), item);
        }
        return map;
    }

    /**
     * 记账行金额里属于「患者自己掏的那部分」：行金额 − 行优惠 − 行统筹。
     *
     * <p>统筹不进柜面的钱箱，退款也不能从柜面退 —— 它由医保局后付，冲正在 2305 报盘侧。
     * 部分红冲（一行退过再退）时按本次冲减额占行原金额的比例摊，保证 Σ摊值 = 行患者实付。
     */
    private BigDecimal patientShare(BizSettlementBillItem item, BigDecimal reversed) {
        if (item == null) {
            return scale(nz(reversed));
        }
        BigDecimal amount = nz(item.getAmount());
        if (amount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal gross = nz(reversed);
        BigDecimal discount = gross.multiply(nz(item.getDiscountAmount())).divide(amount, AMOUNT_SCALE, RoundingMode.HALF_UP);
        BigDecimal pool = gross.multiply(nz(item.getPoolAmount())).divide(amount, AMOUNT_SCALE, RoundingMode.HALF_UP);
        return scale(gross.subtract(discount).subtract(pool));
    }

    private List<Long> resolveRefundFeeIds(List<BizSettlementBillItem> items, List<Long> feeIds) {
        List<Long> allowed = new ArrayList<>();
        for (BizSettlementBillItem item : items) {
            allowed.add(item.getFeeRecordId());
        }
        if (CollectionUtils.isEmpty(feeIds)) {
            if (allowed.isEmpty()) {
                throw new BusinessException("该账单没有账单行，无法退费");
            }
            return allowed;
        }
        for (Long feeId : feeIds) {
            if (!allowed.contains(feeId)) {
                throw new BusinessException("记账行 " + feeId + " 不属于该账单，不能退");
            }
        }
        return feeIds;
    }

    private BizSettlementBill requirePayableBill(Long billId) {
        BizSettlementBill bill = settlementBillService.getById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        BillStatusEnum status = BillStatusEnum.fromCode(bill.getBillStatus());
        if (status == null || !status.payable()) {
            throw new BusinessException("该账单" + BillStatusEnum.descOf(bill.getBillStatus()) + "，不能收款");
        }
        return bill;
    }

    private Long currentCashier() {
        Long employeeId = UserUtils.getCurrentUser().getEmployeeId();
        return employeeId == null ? CASHIER_SYSTEM : employeeId;
    }

    private LambdaQueryWrapper<BizPaymentTxn> buildWrapper(PaymentTxnQueryPageDTO query) {
        String keyword = query.getKeyword();
        return new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(query.getBillId() != null, BizPaymentTxn::getBillId, query.getBillId())
                .eq(query.getPatientId() != null, BizPaymentTxn::getPatientId, query.getPatientId())
                .eq(query.getDirection() != null, BizPaymentTxn::getDirection, query.getDirection())
                .eq(query.getPayMethod() != null, BizPaymentTxn::getPayMethod, query.getPayMethod())
                .eq(query.getTxnStatus() != null, BizPaymentTxn::getTxnStatus, query.getTxnStatus())
                .eq(query.getSourceType() != null, BizPaymentTxn::getSourceType, query.getSourceType())
                .eq(query.getCashierId() != null, BizPaymentTxn::getCashierId, query.getCashierId())
                .ge(query.getBeginDate() != null, BizPaymentTxn::getTxnDate, query.getBeginDate())
                .le(query.getEndDate() != null, BizPaymentTxn::getTxnDate, query.getEndDate())
                // 备注也参与检索：押金类流水（source_type=3 住院预交金）不区分渠道，
                // 「小程序微信充值（支付单 PAY…）」只在备注里 —— 不搜备注，财务就查不出今天小程序收了几笔押金
                .and(StringUtils.hasText(keyword), w -> w.like(BizPaymentTxn::getTxnNo, keyword)
                        .or().like(BizPaymentTxn::getBillNo, keyword)
                        .or().like(BizPaymentTxn::getPatientName, keyword)
                        .or().like(BizPaymentTxn::getChannelTxnNo, keyword)
                        .or().like(BizPaymentTxn::getRemark, keyword))
                .orderByDesc(BizPaymentTxn::getTxnTime)
                .orderByDesc(BizPaymentTxn::getId);
    }
}
