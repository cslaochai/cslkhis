package com.his.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备绑定入参：住院护士站从设备科可申领库存选中设备编号，绑到当前这条在院就诊。
 * <p>
 * 床位/科室/病区<b>不由前端传</b>：服务端按 admissionId 从住院域取当时快照，避免页面里过期的床位数据被写进绑定表。
 */
@Data
@Schema(description = "设备绑定入参")
public class IotDeviceBindDTO implements Serializable {

    @Schema(description = "设备编号（扫码得到，如 PUMP-01）", example = "PUMP-01")
    @NotBlank(message = "设备编号不能为空")
    private String deviceNo;

    @Schema(description = "设备类型（与设备档案核对，防止把气体终端/床垫误绑成输液泵）", example = "infusion")
    private String deviceType;

    @Schema(description = "住院就诊ID（在院患者）", example = "8610000000000000012")
    @NotNull(message = "住院就诊ID不能为空")
    private Long admissionId;

    @Schema(description = "绑定方式（1-扫码 2-手工 3-住院申领），默认扫码", example = "3")
    private Integer bindSource;

    @Schema(description = "备注")
    private String remark;
}
