package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 入库单取消入参
 *
 * 取消原因必填：入库单是凭证，作废必须写清为什么（谁取消、为什么取消要能追溯到）。
 */
@Data
public class DrugInboundCancelDTO {

    @NotNull(message = "入库单ID不能为空")
    private Long inboundId;

    /** 取消原因 */
    @NotBlank(message = "取消原因不能为空")
    @Size(max = 200, message = "取消原因最长 200 位")
    private String cancelReason;
}
