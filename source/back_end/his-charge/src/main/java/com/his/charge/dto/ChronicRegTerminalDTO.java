package com.his.charge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 备案终态操作入参（注销 / 驳回共用；终态不可逆，原因必填）。
 */
@Data
public class ChronicRegTerminalDTO {

    @NotNull(message = "备案ID不能为空")
    private Long id;

    /**
     * 注销/驳回原因
     */
    @NotBlank(message = "原因不能为空")
    private String reason;
}
