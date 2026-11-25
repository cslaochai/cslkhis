package com.his.iot.dto;

import com.his.common.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备最新值查询条件。
 * <p>
 * {@code deviceType} 为空表示 11 类设备全查（运维总览页用）。取值白名单与
 * {@link com.his.iot.support.DeviceTypeSchema} 的注册表逐字对应，新增设备类型时两处一起改。
 */
@Data
@Schema(description = "设备最新值查询")
public class IotLatestQueryDTO implements Serializable {

    @Schema(description = "设备类型，空=全部", example = "infusion")
    @AllowedValues(values = {"coldchain", "infusion", "asset", "env", "gas", "bed",
            "dialysis", "cabinet", "baby", "uv", "robot"}, message = "设备类型取值不合法")
    private String deviceType;
}
