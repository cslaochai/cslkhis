package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调拨单流转操作入参（确认发出 / 确认接收 / 作废）
 */
@Data
public class DrugTransferActionDTO {

    @NotNull(message = "调拨单ID不能为空")
    private Long id;

    /** 作废原因（仅作废必填；确认动作忽略此字段） */
    private String reason;
}
