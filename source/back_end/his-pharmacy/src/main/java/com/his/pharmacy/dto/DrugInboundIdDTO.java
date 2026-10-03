package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 入库单操作入参（审核 / 入库：只需单号）
 */
@Data
public class DrugInboundIdDTO {

    /** 来源入库单ID */
    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;
}
