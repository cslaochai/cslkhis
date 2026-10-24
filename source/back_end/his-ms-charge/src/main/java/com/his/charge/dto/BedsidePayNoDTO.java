package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 床旁结算按支付单操作的入参（轮询收款结果）。
 */
@Data
public class BedsidePayNoDTO {

    /**
     * 支付单号（必填，出码时返回）
     */
    @NotBlank(message = "支付单号不能为空")
    private String payNo;
}
