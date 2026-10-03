package com.his.fee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账行红冲入参（L1 唯一的"改错"出口）。
 *
 * <p>数量为空 = 整行红冲；给了数量 = 部分红冲，只写一条负行，原行金额不动。
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
