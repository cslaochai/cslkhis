package com.his.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 可绑定设备（设备档案里启用、且当前没有任何"在用"绑定的设备）。护士站绑定弹框的候选清单。
 */
@Data
@Schema(description = "可绑定设备")
public class IotBindCandidateDeviceVO implements Serializable {

    @Schema(description = "设备编号", example = "PUMP-01")
    private String deviceNo;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "安装位置/归放位置")
    private String location;

    @Schema(description = "归属科室名称")
    private String deptName;
}
