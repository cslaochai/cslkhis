package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消转科入参（P4.2：仅「待接收」可取消）。
 */
@Data
public class InpatientTransferCancelDTO implements Serializable {

    /**
     * 转科记录ID（必填）
     */
    @NotNull(message = "转科记录ID不能为空")
    private Long transferId;

    /**
     * 取消原因（必填）
     */
    @NotBlank(message = "取消原因不能为空")
    private String cancelReason;
}
