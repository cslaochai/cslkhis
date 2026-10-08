package com.his.charge.service.impl;


import com.his.charge.api.AppointChargeGateway;
import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.service.FeeRecordService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.common.enums.*;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 挂号侧收费能力 SPI 实现，落在 his-charge（四层模型口径，见 AppointChargeGateway）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointChargeGatewayImpl implements AppointChargeGateway {

    /**
     * 挂号费目录类别：0-自费（不参与统筹分摊，与旧口径一致）
     */
    private static final int CATALOG_REGIST_FEE = 0;
    /**
     * 诊查费目录类别：1-甲类（医保按报销比例全额纳入统筹）
     */
    private static final int CATALOG_DIAGNOSIS_FEE = 1;

    private final BizSettlementBillMapper bizSettlementBillMapper;
    private final FeeRecordService feeRecordService;
    private final SettlementBillService settlementBillService;
    private final PaymentService paymentService;

    @Override
    public Map<Long, BillBrief> mapBillsByIds(Collection<Long> billIds) {
        Map<Long, BillBrief> result = new HashMap<>();
        if (billIds == null || billIds.isEmpty()) {
            return result;
        }
        for (BizSettlementBill bill : bizSettlementBillMapper.selectBatchIds(billIds)) {
            result.put(bill.getId(), toBrief(bill));
        }
        return result;
    }

    @Override
    public BillBrief getBill(Long billId) {
        if (billId == null) {
            return null;
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(billId);
        return bill == null ? null : toBrief(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillBrief createRegistCharge(RegistChargeCommand command) {
        if (command == null || command.getRegistId() == null) {
            return null;
        }
        // 免收 = 一分钱都不应收：不记账、不出账单，返回 null。
        // 旧模型在这里造一张 0 元收费单并直接置「已收费」，只为骗过签到的收费单门禁 ——
        // 现在门禁认的是「有没有未结清的挂号账单」，没有账单本身就是答案。
        if (command.isWaived()) {
            log.info("[挂号费] 免收，不记账不出账：registNo={} 依据={}",
                    command.getRegistNo(), command.getWaiveReason());
            return null;
        }

        BigDecimal registFee = NumUtil.orZero(command.getRegistFee());
        BigDecimal diagnosisFee = NumUtil.orZero(command.getDiagnosisFee());
        List<FeeBookDTO> books = new ArrayList<>(2);
        if (registFee.signum() > 0) {
            books.add(buildBook(command, "挂号费", "GHF", registFee, CATALOG_REGIST_FEE));
        }
        if (diagnosisFee.signum() > 0) {
            books.add(buildBook(command, "诊查费", "ZCF", diagnosisFee, CATALOG_DIAGNOSIS_FEE));
        }
        if (books.isEmpty()) {
            // 两项都是 0 却按"未免收"进来：同样是零应收，处理方式与免收一致，别硬出空账单
            return null;
        }
        List<BizFeeRecord> rows = feeRecordService.bookBatch(books);
        List<Long> feeIds = new ArrayList<>(rows.size());
        for (BizFeeRecord row : rows) {
            feeIds.add(row.getId());
        }
        BillSettleUpsertDTO settleDTO = new BillSettleUpsertDTO();
        settleDTO.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        settleDTO.setEncounterId(command.getRegistId());
        settleDTO.setFeeIds(feeIds);
        settleDTO.setBillType(BillTypeEnum.REGISTRATION.getCode());
        // 部分免收（只免挂号费、诊查费照收）时账单仍要缴，但备注里必须留「免了哪一项、依据哪条策略」
        settleDTO.setRemark(TextUtil.hasText(command.getWaiveReason()) ? command.getWaiveReason() : null);
        BizSettlementBill bill = settlementBillService.settle(settleDTO);
        log.info("[挂号费] 记账出账完成 registNo={} 账单={} 应缴 ¥{}",
                command.getRegistNo(), bill.getBillNo(), bill.getPayableAmount().toPlainString());
        return toBrief(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegistCharge(CancelCommand command) {
        if (command == null || command.getBillId() == null) {
            return false;
        }
        boolean closed = paymentService.closeBill(command.getBillId(),
                TextUtil.hasText(command.getReason()) ? command.getReason() : "退号",
                TxnSourceEnum.CANCEL_REGIST.getCode());
        if (closed) {
            log.info("[挂号费] 退号联动整单撤销：billId={} 操作人={}",
                    command.getBillId(), command.getOperator());
        }
        return closed;
    }

    private FeeBookDTO buildBook(RegistChargeCommand command, String itemName, String itemCode,
                                 BigDecimal amount, int catalogType) {
        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(command.getPatientId());
        dto.setPatientNo(command.getPatientNo());
        dto.setPatientName(command.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.OUTPATIENT.getCode());
        dto.setEncounterId(command.getRegistId());
        dto.setEncounterNo(command.getRegistNo());
        dto.setDeptId(command.getDeptId());
        dto.setDeptName(command.getDeptName());
        dto.setDoctorId(command.getDoctorId());
        dto.setDoctorName(command.getDoctorName());
        dto.setItemType(PaymentItemTypeEnum.REGISTRATION_FEE.getCode());
        dto.setItemCode(itemCode);
        dto.setItemName(itemName);
        dto.setUnit("次");
        dto.setPrice(amount);
        dto.setQuantity(BigDecimal.ONE);
        dto.setSourceType(FeeSourceTypeEnum.REGISTRATION.getCode());
        dto.setSourceId(command.getRegistId());
        dto.setSourceNo(command.getRegistNo());
        dto.setCatalogType(catalogType);
        return dto;
    }

    private BillBrief toBrief(BizSettlementBill bill) {
        BillBrief brief = new BillBrief();
        brief.setBillId(bill.getId());
        brief.setBillNo(bill.getBillNo());
        brief.setBillStatus(bill.getBillStatus());
        brief.setPayableAmount(bill.getPayableAmount());
        BigDecimal paid = NumUtil.orZero(bill.getPaidAmount()).subtract(NumUtil.orZero(bill.getRefundAmount()));
        brief.setPaidAmount(paid.signum() < 0 ? BigDecimal.ZERO : paid);
        return brief;
    }
}
