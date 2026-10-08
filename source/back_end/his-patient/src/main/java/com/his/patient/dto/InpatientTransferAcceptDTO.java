package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 接收转科入参（P4.2：**转科在这一刻才真正生效**）。
 */
@Data
public class InpatientTransferAcceptDTO implements Serializable {

    /**
     * 转科记录ID（必填）
     */
    @NotNull(message = "转科记录ID不能为空")
    private Long transferId;

    /**
     * 接收备注（可空；医嘱处置说明由服务层自动写入 order_remark）
     */
    private String remark;
}
