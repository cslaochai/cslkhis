package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 作废医嘱入参。
 */
@Data
public class InpatientOrderCancelDTO implements Serializable {

    /**
     * 医嘱ID（必填）
     */
    @NotNull(message = "医嘱ID不能为空")
    private Long orderId;

    /**
     * 作废原因（必填）
     */
    @NotBlank(message = "作废原因不能为空")
    private String cancelReason;
}
