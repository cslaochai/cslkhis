package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作废退费申请入参
 */
@Data
public class RefundDiscardDTO {

    /**
     * 退费申请ID
     */
    @NotNull(message = "缺少退费申请ID")
    private Long id;

    /**
     * 作废原因（必填：批了又退回去必须留得下"为什么"）
     */
    @NotBlank(message = "请填写作废原因")
    private String reason;
}
