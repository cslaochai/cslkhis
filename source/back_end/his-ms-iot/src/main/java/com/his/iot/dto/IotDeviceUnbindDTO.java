package com.his.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备解绑入参：扫泵身条码解绑（换泵/治疗结束/出院回收）。
 * <p>
 * 只认设备编号：一台设备同时只会有一条"在用"绑定，扫到它就是当前患者那条；不认 bindId，
 * 避免页面拿着过期 id 解掉别人新绑的关系。
 */
@Data
@Schema(description = "设备解绑入参")
public class IotDeviceUnbindDTO implements Serializable {

    @Schema(description = "设备编号", example = "PUMP-01")
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    @Schema(description = "解绑原因（换泵/治疗结束/出院/设备回收）", example = "治疗结束")
    private String reason;
}
