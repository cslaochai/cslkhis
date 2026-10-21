package com.his.clinical.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方流转单动作入参（取药完成/取消）
 */
@Data
public class RxFlowActionDTO {
    /** 流转单ID */
    @NotNull(message = "流转单ID不能为空")
    private Long flowId;
    private String reason;
}
