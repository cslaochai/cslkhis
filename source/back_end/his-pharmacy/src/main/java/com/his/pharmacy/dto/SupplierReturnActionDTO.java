package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 供应商退货单流转操作入参（确认退货 / 作废）
 */
@Data
public class SupplierReturnActionDTO {

    @NotNull(message = "退货单ID不能为空")
    private Long id;

    /** 作废原因（仅作废必填；确认退货忽略此字段） */
    private String reason;
}
