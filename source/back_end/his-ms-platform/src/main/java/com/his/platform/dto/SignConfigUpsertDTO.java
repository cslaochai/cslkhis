package com.his.platform.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"1", "2", "3"}, message = "签名时间来源不合法")
    private Integer timeSource;

    @NotNull(message = "签名证书有效期不能为空")
    @Min(value = 1, message = "签名证书有效期必须大于 0")
    private Integer certValidDays;

    @NotNull(message = "证书自动签发开关不能为空")
    @AllowedValues(values = {"0", "1"}, message = "证书自动签发开关取值不合法")
    private Integer certAutoIssue;

    @NotNull(message = "签名PIN免输入时长不能为空")
    @Min(value = 0, message = "签名PIN免输入时长不能小于 0（0 表示每次签名都要输入）")
    @Max(value = 1440, message = "签名PIN免输入时长不能超过 1440 分钟")
    private Integer pinTtlMinutes;
}
