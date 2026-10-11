package com.his.charge.api;


import com.his.charge.dto.BillSettleUpsertDTO;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.enums.InpatientSettleStatusEnum;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.service.FeeRecordService;
import com.his.charge.service.FundAccountService;
import com.his.charge.service.PaymentService;
import com.his.charge.service.SettlementBillService;
import com.his.common.enums.*;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.system.api.service.ChargeApi;
import com.his.system.api.vo.BillBriefVO;
import com.his.system.api.vo.CancelCommandVO;
import com.his.system.api.vo.RegistChargeCommandVO;
import com.his.system.api.vo.SettlementStateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * ChargeApi 在 his-charge 侧的实现（四层模型口径）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeApiImpl implements ChargeApi {

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

    private final FundAccountService fundAccountService;

    @Override
    public Map<Long, BillBriefVO> mapBillsByIds(Collection<Long> billIds) {
        Map<Long, BillBriefVO> result = new HashMap<>();
        if (billIds == null || billIds.isEmpty()) {
            return result;
        }
        for (BizSettlementBill bill : bizSettlementBillMapper.selectBatchIds(billIds)) {
            result.put(bill.getId(), toBrief(bill));
        }
        return result;
    }

    @Override
    public BillBriefVO getBill(Long billId) {
        if (billId == null) {
            return null;
        }
        BizSettlementBill bill = bizSettlementBillMapper.selectById(billId);
        return bill == null ? null : toBrief(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillBriefVO createRegistCharge(RegistChargeCommandVO command) {
        if (command == null || command.getRegistId() == null) {
            return null;
        }
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
    public boolean cancelRegistCharge(CancelCommandVO command) {
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

    @Override
    public SettlementStateVO state(Long admissionId) {
        SettlementStateVO state = new SettlementStateVO();
        if (admissionId == null) {
            state.setText("入院ID为空，无法校验结算状态");
            return state;
        }

        BizSettlementBill discharge = settlementBillService.latestDischargeBill(admissionId);
        BigDecimal balance = NumUtil.orZero(fundAccountService.admissionBalance(admissionId));
        if (discharge == null) {
            // 未结算也要把预交金余额带回去：前台靠这句话判断"是没钱结、还是压根没办结算"
            state.setSettled(false);
            state.setArrearsAmount(BigDecimal.ZERO);
            state.setText("尚未办理住院结算（预交金余额 " + balance.toPlainString() + " 元）");
            return state;
        }

        boolean paidOff = isPaidOff(discharge);
        state.setSettled(true);
        state.setSettlementNo(discharge.getBillNo());
        state.setArrearsAmount(paidOff ? BigDecimal.ZERO : NumUtil.orZero(discharge.getPayableAmount()).subtract(NumUtil.orZero(discharge.getPaidAmount())));
        // settle_status 是旧表字段语义，四层后由账单状态推导：1-已结清 2-欠费
        state.setSettleStatus(paidOff ? InpatientSettleStatusEnum.CLEARED.getCode() : InpatientSettleStatusEnum.ARREARS.getCode());
        state.setText("已结算（" + (paidOff ? "已结清" : "欠费 " + state.getArrearsAmount().toPlainString() + " 元")
                + "，账单号 " + discharge.getBillNo() + "，" + BillStatusEnum.descOf(discharge.getBillStatus())
                + "，预交金余额 " + balance.toPlainString() + " 元）");
        return state;
    }

    /**
     * 是否已付清：应缴为 0 也算付清（预交金全额抵扣后账单应缴就是 0，没有流水但确实结清了）。
     */
    private boolean isPaidOff(BizSettlementBill bill) {
        return NumUtil.orZero(bill.getPayableAmount()).subtract(NumUtil.orZero(bill.getPaidAmount())).compareTo(BigDecimal.ZERO) <= 0;
    }

    private FeeBookDTO buildBook(RegistChargeCommandVO command, String itemName, String itemCode,
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

    private BillBriefVO toBrief(BizSettlementBill bill) {
        BillBriefVO brief = new BillBriefVO();
        brief.setBillId(bill.getId());
        brief.setBillNo(bill.getBillNo());
        brief.setBillStatus(bill.getBillStatus());
        brief.setPayableAmount(bill.getPayableAmount());
        BigDecimal paid = NumUtil.orZero(bill.getPaidAmount()).subtract(NumUtil.orZero(bill.getRefundAmount()));
        brief.setPaidAmount(paid.signum() < 0 ? BigDecimal.ZERO : paid);
        return brief;
    }
}
