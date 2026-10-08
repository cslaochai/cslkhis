package com.his.charge.service.impl;

import com.his.charge.api.InpatientSettlementGateway;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.enums.InpatientSettleStatusEnum;
import com.his.charge.service.FundAccountService;
import com.his.charge.service.SettlementBillService;
import com.his.common.enums.BillStatusEnum;
import com.his.common.util.NumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * InpatientSettlementGateway 的实现，落在 his-charge。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientSettlementGatewayImpl implements InpatientSettlementGateway {

    private final SettlementBillService settlementBillService;
    private final FundAccountService fundAccountService;

    @Override
    public SettlementState state(Long admissionId) {
        SettlementState state = new SettlementState();
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
}
