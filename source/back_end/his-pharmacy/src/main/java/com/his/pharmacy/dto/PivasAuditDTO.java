package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 静配明细审方入参。
 */
@Data
public class PivasAuditDTO implements Serializable {

    /**
     * 静配明细ID
     */
    @NotNull(message = "明细ID不能为空")
    private Long itemId;

    /**
     * 审方结论：true-通过 false-退回
     */
    @NotNull(message = "审方结论不能为空")
    private Boolean pass;

    /**
     * 退回原因（pass=false 必填）
     */
    private String reason;
}
