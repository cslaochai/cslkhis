package com.his.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * TSA 服务启停入参（G6b 运维）。
 */
@Data
public class TsaStatusUpsertDTO {

    /**
     * 目标状态（0-停用 1-启用，his_enable_status）
     */
    @NotNull(message = "目标状态不能为空")
    private Integer tsaStatus;
}
