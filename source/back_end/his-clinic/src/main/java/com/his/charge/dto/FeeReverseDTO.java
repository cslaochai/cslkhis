package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账行红冲入参（L1 唯一的"改错"出口）。
 */
@Data
public class FeeReverseDTO {

    @NotNull(message = "缺少记账行")
    private Long feeId;

    /**
     * 本次冲减数量（正数，不得超过本行剩余数量）；为空表示整行红冲
     */
    private BigDecimal quantity;

    /**
     * 冲减原因：错账没有原因就是审计上的黑洞，必填
     */
    @NotBlank(message = "缺少红冲原因")
    private String reason;
}
