package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 等床队列动作入参（退回队列 / 取消排队共用）
 */
@Data
public class BedWaitOperateDTO {

    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /**
     * 原因
     */
    private String reason;
}
