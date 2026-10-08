package com.his.medicaltech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消输血申请入参。
 */
@Data
public class TransfusionCancelDTO implements Serializable {

    /**
     * 输血申请单ID（必填）
     */
    @NotNull(message = "输血申请单ID不能为空")
    private Long applyId;

    /**
     * 取消原因（必填：停止用血是一个临床决定，必须有人负责、有理由）
     */
    @NotBlank(message = "取消原因不能为空（停止用血是一个临床决定，必须有人负责）")
    private String cancelReason;
}
