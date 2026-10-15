package com.his.emr.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 代煎状态推进入参（只进不退：1-待煎 → 2-已煎 → 3-已取）
 */
@Data
public class TcmDecoctAdvanceDTO {

    @NotNull(message = "代煎单ID不能为空")
    private Long id;

    /**
     * 目标状态（2-已煎 3-已取；9 走 /cancel，1 是初始态不可回退）
     */
    @NotNull(message = "目标状态不能为空")
    @AllowedValues(values = {"2", "3"}, message = "目标状态取值不合法")
    private Integer targetStatus;
}
