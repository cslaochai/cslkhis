package com.his.platform.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 签名时间来源切换入参（G6b 运维）。
 */
@Data
public class TsaTimeSourceDTO {

    /**
     * 时间来源（1-本机时钟 2-院内授时服务器 3-第三方TSA）
     */
    @NotNull(message = "时间来源不能为空")
    @AllowedValues(values = {"1", "2", "3"}, message = "时间来源不合法")
    private Integer timeSource;
}
