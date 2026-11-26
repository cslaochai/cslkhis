package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备档案查询条件（按类型取清单，设备编号可选精确过滤）。
 */
@Data
@Schema(description = "物联网设备档案查询")
public class IotDeviceQueryDTO implements Serializable {

    @Schema(description = "设备类型，空=全部", example = "coldchain")
    @AllowedValues(values = {"coldchain", "infusion", "asset", "env", "gas", "bed",
            "dialysis", "cabinet", "uv", "robot"}, message = "设备类型取值不合法")
    private String deviceType;

    @Schema(description = "设备编号，空=全部", example = "FRIDGE-01")
    private String deviceNo;
}
