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
 * {@link InpatientSettlementGateway} 的实现，落在 his-charge。
 *
 * <p>只回答"有没有出院结算、是什么状态"，<b>不做任何拦截判断</b> ——
 * 拦不拦由调用方（出院办理）决定，这里给的是事实。
 *
 * <p><b>出院结算的事实就是一张 {@code bill_type=4} 的账单</b>（L2）：旧表
 * 住院结算单那份"结算台账"已退役，同一件事不许有两份记录 ——
 * 两份记录必然漂移，出院门禁就会读到过期那一份。
 * 欠费额按账单现算（{@code payable_amount - paid_amount}），因为实收权威在
 * 支付资金流水，账单上的 {@code paid_amount} 只是流水的镜像。
 *
 * <p>已作废账单不算结算：作废意味着"这次结算被撤销、要重结"，
 * 若它算数就会出现"有作废结算单所以放行出院、但账上分文未收"。
 *
 * <p>刻意不调用 {@code InpatientAccountService.summary}：那个接口会在欠费时写告警，
 * 而"出院前查一下状态"这种高频只读查询不该有副作用。
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
