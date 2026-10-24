package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 床旁结算按账单操作的入参（出码 / 关码）。
 */
@Data
public class BedsideBillDTO {

    /**
     * 结算账单ID（必填）
     */
    @NotNull(message = "账单ID不能为空")
    private Long billId;
}
