package com.his.clinical.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 医生自助补签入参：只能补签本人书写、尚未电子签名的对象。
 * 补签必须写原因，并输入本人的签名 PIN；服务端强制签名人取当前登录医生，不接受前端指定。
 */
@Data
@Schema(description = "医生自助补签入参")
public class MyMakeupSignDTO {

    /**
     * 签名对象类型（2-门诊病历 4-处方 7-检查申请单 8-检验申请单）
     */
    @Schema(description = "签名对象类型")
    @NotNull(message = "签名对象类型不能为空")
    private Integer bizType;

    /**
     * 签名对象ID
     */
    @Schema(description = "签名对象ID")
    @NotNull(message = "签名对象ID不能为空")
    private Long bizId;

    /**
     * 补签原因（必填，写进签名备注与审计日志）
     */
    @Schema(description = "补签原因")
    @NotBlank(message = "补签原因不能为空")
    @Size(max = 200, message = "补签原因不能超过200个字符")
    private String reason;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    @Schema(description = "签名PIN")
    @NotBlank(message = "签名PIN不能为空")
    @Size(max = 32, message = "签名PIN不能超过32个字符")
    private String pin;
}
