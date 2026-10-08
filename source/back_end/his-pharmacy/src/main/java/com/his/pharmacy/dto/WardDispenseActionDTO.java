package com.his.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 摆药明细操作入参（配药 / 核对 / 退药）。
 */
@Data
public class WardDispenseActionDTO implements Serializable {

    /**
     * 摆药明细ID
     */
    @NotNull(message = "明细ID不能为空")
    private Long itemId;

    /**
     * 备注（核对选填）
     */
    private String remark;

    /**
     * 退药原因（退药必填）
     */
    private String reason;
}
