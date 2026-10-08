package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方审核入参。
 */
@Data
@Schema(description = "处方审核入参")
public class DrugAuditExecuteDTO {

    /**
     * 处方ID
     */
    @NotNull(message = "处方ID不能为空")
    @Schema(description = "处方ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long prescriptionId;

    @Schema(description = "是否把审核结果写入 biz_clinical_rule_check，默认 true")
    private Boolean saveResult;
}
