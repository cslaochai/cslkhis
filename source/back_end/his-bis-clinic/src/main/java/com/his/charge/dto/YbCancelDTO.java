package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用作废入参（飞检批次作废 / 扣款通知作废）：理由必填，便于事后追溯"为什么这张单没了"。
 */
@Data
public class YbCancelDTO {

    @NotNull(message = "单据ID不能为空")
    private Long id;

    /**
     * 作废原因
     */
    @NotBlank(message = "作废原因不能为空")
    private String reason;
}
