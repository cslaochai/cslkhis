package com.his.charge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 录入缴回（仅「维持扣款待缴」可录；缴回金额必须等于扣款金额，差额走财务另行流程）。
 */
@Data
public class DeductPaybackDTO {

    @NotNull(message = "扣款通知ID不能为空")
    private Long id;

    /**
     * 实际缴回金额
     */
    @NotNull(message = "缴回金额不能为空")
    private BigDecimal paidAmount;

    /**
     * 缴回日期
     */
    @NotNull(message = "缴回日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paybackDate;

    /**
     * 缴回凭证号/转账流水
     */
    @NotBlank(message = "缴回凭证号不能为空")
    private String paybackVoucher;
}
