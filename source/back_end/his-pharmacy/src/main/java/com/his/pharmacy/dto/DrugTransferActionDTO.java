package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调拨单流转操作入参（确认发出 / 确认接收 / 作废）
 *
 * <p>三个动作都只认单号，数量与批次全部以明细为准：让前端二次传数量会出现
 * 「点确认时改了数」，而库存已经按原明细锁定过了。
 */
@Data
public class DrugTransferActionDTO {

    @NotNull(message = "调拨单ID不能为空")
    private Long id;

    /** 作废原因（仅作废必填；确认动作忽略此字段） */
    private String reason;
}
