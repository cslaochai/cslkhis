package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 绑定关系查询条件。deviceType 为空=全部类型。
 */
@Data
@Schema(description = "设备绑定关系查询")
public class IotBindQueryDTO implements Serializable {

    @Schema(description = "设备类型，空=全部", example = "infusion")
    @AllowedValues(values = {"coldchain", "infusion", "asset", "env", "gas", "bed", "monitor",
            "dialysis", "cabinet", "uv", "robot"}, message = "设备类型取值不合法")
    private String deviceType;
}
