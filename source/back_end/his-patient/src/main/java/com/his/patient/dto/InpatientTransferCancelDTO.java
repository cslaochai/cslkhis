package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 取消转科入参（P4.2：仅「待接收」可取消）。
 *
 * <p>已接收的转科是**已经发生过的临床行为**，不允许用取消把它抹掉；
 * 要转回去就再发起一次，轨迹留两条 —— 这才是事实。
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
