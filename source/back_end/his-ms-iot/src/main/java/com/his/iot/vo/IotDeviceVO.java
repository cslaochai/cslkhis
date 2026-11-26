package com.his.iot.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备档案下拉/清单（设备编号、名称、归属与位置）。实时值仍由遥测接口提供。
 */
@Data
@Schema(description = "物联网设备档案")
public class IotDeviceVO implements Serializable {

    @Schema(description = "设备编号", example = "FRIDGE-01")
    private String deviceNo;

    @Schema(description = "设备类型", example = "coldchain")
    private String deviceType;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "归属科室")
    private String deptName;

    @Schema(description = "安装位置")
    private String location;

    @Schema(description = "固定安装床位号（仅床垫/在床监测类设备）")
    private String bedNo;

    @Schema(description = "资产标签绑定的设备台账ID（仅 asset 类型，Long 防精度丢失）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long equipmentId;

    @Schema(description = "启用状态（1-启用 0-停用）")
    private Integer enabled;
}
