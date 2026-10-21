package com.his.spi.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * TSA 服务启停入参（运维）。
 */
@Data
public class TsaStatusUpsertDTO {

    /**
     * 目标状态（0-停用 1-启用）
     */
    @NotNull(message = "目标状态不能为空")
    @AllowedValues(values = {"0", "1"}, message = "目标状态取值不合法")
    private Integer tsaStatus;
}
