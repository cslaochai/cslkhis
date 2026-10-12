package com.his.system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 电子签名 / 可信时间戳参数配置 upsert DTO（sys_config：sign.* 键）
 */
@Data
public class SignConfigUpsertDTO {

    @NotNull(message = "签名时间来源不能为空")
    @Min(value = 1, message = "签名时间来源仅支持 1-本机时钟 2-院内授时 3-第三方 TSA")
    @Max(value = 3, message = "签名时间来源仅支持 1-本机时钟 2-院内授时 3-第三方 TSA")
    private Integer timeSource;

    @NotNull(message = "签名证书有效期不能为空")
    @Min(value = 1, message = "签名证书有效期必须大于 0")
    private Integer certValidDays;

    @NotNull(message = "证书自动签发开关不能为空")
    @Min(value = 0, message = "证书自动签发开关仅支持 0-否 1-是")
    @Max(value = 1, message = "证书自动签发开关仅支持 0-否 1-是")
    private Integer certAutoIssue;
}
