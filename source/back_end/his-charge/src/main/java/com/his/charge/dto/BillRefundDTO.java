package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 按账单退费入参：应收侧红冲记账行 + 资金侧按原收款流水逐笔退回，同一个事务。
 *
 * <p>退多少不由前端填金额：金额永远是"被红冲记账行的净额之和"。
 * 让调用方传金额，等于允许"账上退 100、记账行只冲 60"这种对不上的账。
 */
@Data
public class BillRefundDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;

    /**
     * 要退的记账行ID（费用记账流水的ID）；为空 = 本账单全部行（整单退）
     */
    private List<Long> feeIds;

    /**
     * 原因
     */
    @NotBlank(message = "缺少退费原因")
    private String reason;

    /**
     * 流水来源（字典 his_txn_source）：为空按 5-收费处直退；退费申请执行传 4，退号联动传 6
     */
    private Integer sourceType;

    /**
     * 来源退费申请ID（直退留空）
     */
    private Long applyId;

    /**
     * 来源退费申请号
     */
    private String applyNo;
}
