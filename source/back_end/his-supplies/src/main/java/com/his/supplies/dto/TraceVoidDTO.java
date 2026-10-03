package com.his.supplies.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 高值耗材溯源台账作废入参（退货：置作废并把 1 件加回批次，已计费不允许作废）
 */
@Data
public class TraceVoidDTO {
    /** 台账ID */
    @NotNull(message = "缺少台账ID")
    private Long traceId;
    /** 作废原因 */
    @NotBlank(message = "作废原因不能为空")
    private String reason;
}
