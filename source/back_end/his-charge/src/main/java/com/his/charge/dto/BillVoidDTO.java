package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 取消结算（账单作废）入参：只解锁记账行、不动资金。
 */
@Data
public class BillVoidDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;

    /**
     * 原因
     */
    @NotBlank(message = "缺少作废原因")
    private String reason;
}
