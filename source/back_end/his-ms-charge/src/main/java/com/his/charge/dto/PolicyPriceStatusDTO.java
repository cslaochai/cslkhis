package com.his.charge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 政策价档位启停入参（价格管理页「政策价」页签）。
 */
@Data
public class PolicyPriceStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 价目行主键（sys_charge_policy_price.id） */
    @NotNull(message = "价目行不能为空")
    private Long priceId;

    /** 目标状态：0-停用 1-启用 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态码值非法")
    @Max(value = 1, message = "状态码值非法")
    private Integer status;
}
