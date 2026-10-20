package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按账单操作的入参（关闭该账单上所有渠道的待支付码）。
 */
@Data
public class QrPayBillDTO {

    /**
     * 结算账单ID
     */
    @NotNull(message = "缺少账单")
    private Long billId;
}
