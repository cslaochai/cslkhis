package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 住院出院结算入参。
 *
 * <p><b>只允许传"这次结算的口径"，不允许传钱</b>：应收、统筹、应缴一律由 L2 按选中的记账行现算，
 * 医保类型也不在这里传（由患者档案的参保号推出，见 {@code SettlementBillService}），
 * 否则前端传什么就结算什么，小票与账单必然对不上。
 */
@Data
public class InpatientSettlementUpsertDTO {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 结算方式：1-自费 2-医保（不传按患者有无参保号自动判定）
     */
    private Integer settleMode;

    /**
     * 备注（欠费结算建议写清欠费原因）
     */
    private String remark;
}
