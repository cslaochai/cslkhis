package com.his.platform.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备状态变更入参（停用/启用/维修中/报废）。
 */
@Data
public class EquipmentStatusDTO implements Serializable {

    @NotNull(message = "设备ID不能为空")
    private Long id;

    /**
     * 目标状态（1-在用 2-停用 3-维修中 4-报废）
     */
    @NotNull(message = "设备状态不能为空")
    @AllowedValues(values = {"1", "2", "3", "4"}, message = "设备状态取值不合法")
    private Integer status;
}
