package com.his.platform.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设备共享池目录维护入参。
 */
@Data
public class EquipmentRentableDTO {

    @NotNull(message = "设备ID不能为空")
    private Long id;

    @NotNull(message = "共享池标志不能为空")
    @AllowedValues(values = {"0", "1"}, message = "共享池标志取值不合法")
    private Integer rentable;
}
