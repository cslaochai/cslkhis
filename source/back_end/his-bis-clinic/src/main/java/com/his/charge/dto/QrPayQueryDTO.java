package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按支付单号操作的入参（轮询收款结果、关单共用）。
 */
@Data
public class QrPayQueryDTO {

    /**
     * 支付单号
     */
    @NotBlank(message = "缺少支付单号")
    private String payNo;
}
