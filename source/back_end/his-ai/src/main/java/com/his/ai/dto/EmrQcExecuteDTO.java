package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病历内涵质控入参。
 */
@Data
@Schema(description = "病历内涵质控入参")
public class EmrQcExecuteDTO {

    @NotNull(message = "病历ID不能为空")
    @Schema(description = "病历ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long recordId;

    @Schema(description = "是否把质控结果写入 biz_quality_control，默认 true")
    private Boolean saveResult;
}
