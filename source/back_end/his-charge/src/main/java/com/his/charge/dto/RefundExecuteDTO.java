package com.his.charge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 执行退费入参
 */
@Data
public class RefundExecuteDTO {

    /**
     * 退费申请ID
     */
    @NotNull(message = "缺少退费申请ID")
    private Long id;

    /**
     * 退费执行人
     */
    private String refundBy;

}
