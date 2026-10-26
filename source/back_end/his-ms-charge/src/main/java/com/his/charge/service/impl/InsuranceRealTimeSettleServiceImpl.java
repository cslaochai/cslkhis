package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.entity.BizSettlementBillItem;
import com.his.charge.mapper.BizPaymentTxnMapper;
import com.his.charge.mapper.BizSettlementBillItemMapper;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.service.InsuranceChannelService;
import com.his.charge.service.InsuranceRealTimeSettleService;
import com.his.charge.service.SettlementBillService;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.FeeCatalogTypeEnum;
import com.his.common.enums.PayDirectionEnum;
import com.his.common.enums.PayTxnStatusEnum;
import com.his.common.enums.PaymentMethodEnum;
import com.his.common.enums.TxnSourceEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.PatientBriefVO;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 门诊医保实时结算实现：2201 预结算、2301 正式结算（扣个账 + 回写账单 splits）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceRealTimeSettleServiceImpl implements InsuranceRealTimeSettleService {

    private static final int AMOUNT_SCALE = 2;

    private final BizSettlementBillMapper billMapper;
    private final BizSettlementBillItemMapper billItemMapper;
    private final BizPaymentTxnMapper txnMapper;
    private final InsuranceChannelService insuranceChannelService;
    private final SettlementBillService settlementBillService;
    private final PatientFacadeService patientFacadeService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public InsuranceChannelService.SettleSplit preSettle(Long billId) {
        BizSettlementBill bill = requireBill(billId);
        List<BizSettlementBillItem> items = billItemMapper.selectByBill(billId);
        PatientBriefVO patient = patientFacadeService.findPatient(bill.getPatientId());
        InsuranceChannelService.SettleRequest req = buildRequest(bill, items, patient, bill.getBillNo() + "-2201");
        InsuranceChannelService.SettleSplit split = insuranceChannelService.preSettle(req);
        if (split == null || split.getInfcode() == null || split.getInfcode() != 0) {
            throw new BusinessException("医保预结算失败：" + TextUtil.cut(split == null ? "无响应" : split.getErrMsg(), 200));
        }
        log.info("[医保预结算] 账单 {} 返回 splits：统筹 ¥{} 个账 ¥{} 自付 ¥{}",
                bill.getBillNo(), NumUtil.plain(split.getInsurancePay()),
                NumUtil.plain(split.getPersonalAccount()), NumUtil.plain(split.getCashPay()));
        return split;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InsuranceChannelService.SettleSplit formalSettle(Long billId) {
        BizSettlementBill bill = requireBill(billId);
        // 幂等：已经做过 2301 就不重复扣个账
        BizPaymentTxn existing = txnMapper.selectOne(new LambdaQueryWrapper<BizPaymentTxn>()
                .eq(BizPaymentTxn::getBillId, billId)
                .eq(BizPaymentTxn::getPayMethod, PaymentMethodEnum.INSURANCE_ACCOUNT.getCode())
                .eq(BizPaymentTxn::getTxnStatus, PayTxnStatusEnum.SUCCESS.getCode())
                .eq(BizPaymentTxn::getDirection, PayDirectionEnum.CHARGE.getCode())
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException("该账单已完成医保个账结算（个账流水号 " + existing.getTxnNo() + "），请勿重复结算");
        }

        List<BizSettlementBillItem> items = billItemMapper.selectByBill(billId);
        PatientBriefVO patient = patientFacadeService.findPatient(bill.getPatientId());
        // 2201 用 billNo-2301 作 tradeNo，医保侧按 tradeNo 幂等
        InsuranceChannelService.SettleRequest req = buildRequest(bill, items, patient, bill.getBillNo() + "-2301");
        InsuranceChannelService.SettleSplit split = insuranceChannelService.formalSettle(req);
        if (split == null || split.getInfcode() == null || split.getInfcode() != 0) {
            throw new BusinessException("医保正式结算失败：" + TextUtil.cut(split == null ? "无响应" : split.getErrMsg(), 200));
        }
        if (NumUtil.orZero(split.getPersonalAccount()).signum() <= 0) {
            // 个账为 0 说明这单不需要刷个账（全统筹或全自付），不落个账流水，只回写 splits
            log.info("[医保正式结算] 账单 {} 个账为 0（{}），只回写 splits 不落个账流水", bill.getBillNo(),
                    NumUtil.plain(split.getCashPay()));
        } else {
            insertAccountTxn(bill, split);
        }
        // 回写账单头 + 行级 splits
        writeBackBill(bill, items, split);
        // 刷新账单 paid_amount / 状态 / pay_time（跟现金/扫码收款走同一套逻辑）
        settlementBillService.refreshFromTxns(billId);
        log.info("[医保正式结算] 账单 {} 完成：统筹 ¥{} 个账 ¥{} 现金 ¥{} 医保单号 {}",
                bill.getBillNo(), NumUtil.plain(split.getInsurancePay()),
                NumUtil.plain(split.getPersonalAccount()), NumUtil.plain(split.getCashPay()),
                split.getReceiptNo());
        return split;
    }

    /**
     * 组 2201/2301 请求：患者身份 + 账单行费用明细。
     */
    private InsuranceChannelService.SettleRequest buildRequest(BizSettlementBill bill,
                                                               List<BizSettlementBillItem> items,
                                                               PatientBriefVO patient, String tradeNo) {
        InsuranceChannelService.SettleRequest req = new InsuranceChannelService.SettleRequest();
        req.setTradeNo(tradeNo);
        req.setBillNo(bill.getBillNo());
        req.setEncounterType(bill.getEncounterType() == null ? null : String.valueOf(bill.getEncounterType()));
        req.setIdCard(patient == null ? null : patient.getIdCard());
        req.setInsuranceNo(patient == null ? null : patient.getMedicalInsuranceNo());
        req.setPatientName(bill.getPatientName());
        List<InsuranceChannelService.FeeItem> feeItems = new ArrayList<>(items.size());
        for (BizSettlementBillItem row : items) {
            InsuranceChannelService.FeeItem fi = new InsuranceChannelService.FeeItem();
            fi.setYbCode(row.getItemCode());
            fi.setName(row.getItemName());
            fi.setPayClass(payClassOf(row.getCatalogType()));
            fi.setPrice(NumUtil.orZero(row.getPrice()));
            fi.setQty(row.getQuantity() == null ? null : row.getQuantity().intValue());
            feeItems.add(fi);
        }
        req.setItems(feeItems);
        return req;
    }

    /**
     * 目录类别 code → 医保平台要的短名（甲/乙/丙/自费）。
     */
    private String payClassOf(Integer catalogType) {
        FeeCatalogTypeEnum type = FeeCatalogTypeEnum.fromCode(catalogType);
        if (type == null) {
            return "自费";
        }
        return switch (type) {
            case CLASS_A -> "甲";
            case CLASS_B -> "乙";
            case CLASS_C -> "丙";
            case SELF_PAY -> "自费";
        };
    }

    /**
     * 落一笔医保个账收款流水（pay_method=4, direction=1）。
     */
    private void insertAccountTxn(BizSettlementBill bill, InsuranceChannelService.SettleSplit split) {
        BizPaymentTxn txn = new BizPaymentTxn();
        txn.setTxnNo("PT" + redisSequenceService.next("PT"));
        txn.setBillId(bill.getId());
        txn.setBillNo(bill.getBillNo());
        txn.setPatientId(bill.getPatientId());
        txn.setPatientNo(bill.getPatientNo());
        txn.setPatientName(bill.getPatientName());
        txn.setEncounterType(bill.getEncounterType());
        txn.setEncounterId(bill.getEncounterId());
        txn.setDirection(PayDirectionEnum.CHARGE.getCode());
        txn.setPayMethod(PaymentMethodEnum.INSURANCE_ACCOUNT.getCode());
        txn.setAmount(NumUtil.orZero(split.getPersonalAccount()));
        txn.setTxnStatus(PayTxnStatusEnum.SUCCESS.getCode());
        txn.setSourceType(TxnSourceEnum.CASHIER.getCode());
        txn.setChannelTxnNo(split.getReceiptNo());
        txn.setReceiptNo(split.getReceiptNo());
        txn.setCashierId(UserUtils.getCurrentUser().getEmployeeId());
        txn.setCashierName(UserUtils.getCurrentUser().getRealName());
        txn.setTxnTime(LocalDateTime.now());
        txn.setTxnDate(LocalDate.now());
        txn.setRemark("医保个账划扣（2301），医保余额扣后 ¥" + NumUtil.plain(split.getAccountBalanceAfter()));
        txnMapper.insert(txn);
    }

    /**
     * 回写账单头 + 行级 splits。
     *
     * <p>行级 splits 按每行净额（amount - discountAmount）占比分摊 mss 返回的整三段，
     * 最后一行倒挤保证 Σ行 = 整单。mss 返回的是整单 splits，没有逐行明细，这是已知的近似。
     */
    private void writeBackBill(BizSettlementBill bill, List<BizSettlementBillItem> items,
                                InsuranceChannelService.SettleSplit split) {
        BigDecimal netTotal = BigDecimal.ZERO;
        for (BizSettlementBillItem row : items) {
            netTotal = netTotal.add(NetAmount.net(row));
        }
        if (netTotal.signum() <= 0) {
            throw new BusinessException("账单净额为 0，无法回写医保 splits");
        }
        BigDecimal leftPool = NumUtil.orZero(split.getInsurancePay());
        BigDecimal leftAccount = NumUtil.orZero(split.getPersonalAccount());
        BigDecimal leftSelf = NumUtil.orZero(split.getSelfPay());
        BigDecimal totalPool = leftPool;
        BigDecimal totalAccount = leftAccount;
        BigDecimal totalSelf = leftSelf;
        for (int i = 0; i < items.size(); i++) {
            BizSettlementBillItem row = items.get(i);
            BigDecimal net = NetAmount.net(row);
            BigDecimal ratio = net.divide(netTotal, 6, RoundingMode.HALF_UP);
            BigDecimal pool = i == items.size() - 1 ? leftPool
                    : NumUtil.scale(totalPool.multiply(ratio), AMOUNT_SCALE);
            BigDecimal account = i == items.size() - 1 ? leftAccount
                    : NumUtil.scale(totalAccount.multiply(ratio), AMOUNT_SCALE);
            BigDecimal self = i == items.size() - 1 ? leftSelf
                    : NumUtil.scale(totalSelf.multiply(ratio), AMOUNT_SCALE);
            leftPool = leftPool.subtract(pool);
            leftAccount = leftAccount.subtract(account);
            leftSelf = leftSelf.subtract(self);
            row.setPoolAmount(pool);
            row.setAccountAmount(account);
            row.setSelfAmount(self);
            billItemMapper.updateById(row);
        }
        // 账单头回写：pool/account/self + payable = total - discount - pool - account
        BigDecimal total = NumUtil.orZero(bill.getTotalAmount());
        BigDecimal discount = NumUtil.orZero(bill.getDiscountAmount());
        BigDecimal pool = NumUtil.orZero(split.getInsurancePay());
        BigDecimal account = NumUtil.orZero(split.getPersonalAccount());
        bill.setPoolAmount(pool);
        bill.setAccountAmount(account);
        bill.setSelfAmount(NumUtil.orZero(split.getSelfPay()));
        bill.setPayableAmount(NumUtil.scale(total.subtract(discount).subtract(pool).subtract(account), AMOUNT_SCALE));
        billMapper.updateById(bill);
    }

    private BizSettlementBill requireBill(Long billId) {
        if (billId == null) {
            throw new BusinessException("缺少账单ID");
        }
        BizSettlementBill bill = billMapper.selectById(billId);
        if (bill == null) {
            throw new BusinessException("结算账单不存在");
        }
        BillStatusEnum status = BillStatusEnum.fromCode(bill.getBillStatus());
        if (status == BillStatusEnum.VOIDED) {
            throw new BusinessException("账单 " + bill.getBillNo() + " 已作废，不能做医保结算");
        }
        // 校验：只有医保联网结算方式的账单才调 2201/2301；自费/公费医疗/离休走别的通道
        if (bill.getSettlementMode() == null || bill.getSettlementMode() != 2) {
            throw new BusinessException("账单 " + bill.getBillNo() + " 不是医保结算方式，不能调医保实时结算");
        }
        String type = bill.getInsuranceType();
        if (TextUtil.hasText(type) && type.contains("自费")) {
            throw new BusinessException("账单 " + bill.getBillNo() + " 费别为自费，不能调医保实时结算");
        }
        return bill;
    }

    /** 行净额 = amount - discountAmount（分摊基数）。 */
    private static final class NetAmount {
        static BigDecimal net(BizSettlementBillItem row) {
            return NumUtil.orZero(row.getAmount()).subtract(NumUtil.orZero(row.getDiscountAmount()));
        }
    }
}
