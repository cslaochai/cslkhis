package com.his.iot.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备—患者绑定关系（biz_iot_device_bind）的出参。
 * <p>
 * 护士站用 admissionId 把绑定挂到床位图上的患者（不按床位挂：转床后泵跟着人走）。
 */
@Data
@Schema(description = "设备绑定关系")
public class IotDeviceBindVO implements Serializable {

    @Schema(description = "绑定记录ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "设备编号", example = "PUMP-01")
    private String deviceNo;

    @Schema(description = "设备类型", example = "infusion")
    private String deviceType;

    @Schema(description = "设备名称", example = "输液泵 PUMP-01")
    private String deviceName;

    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    @Schema(description = "住院就诊ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    @Schema(description = "绑定时的床位ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    @Schema(description = "绑定时的床位号")
    private String bedNo;

    @Schema(description = "绑定时的病区ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    @Schema(description = "绑定时的科室ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    @Schema(description = "绑定方式（1-扫码 2-手工 3-住院申领）")
    private Integer bindSource;

    @Schema(description = "绑定方式文案")
    private String bindSourceText;

    @Schema(description = "绑定人")
    private String bindBy;

    @Schema(description = "绑定时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bindTime;
}
