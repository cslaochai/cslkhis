package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作废住院证入参
 */
@Data
public class AdmissionOrderCancelDTO {

    /**
     * 住院证ID（必填）
     */
    @NotNull(message = "住院证ID不能为空")
    private Long id;

    /**
     * 作废原因（必填——作废必须有人负责，不能无声消失）
     */
    @NotBlank(message = "作废原因不能为空")
    private String cancelReason;
}
