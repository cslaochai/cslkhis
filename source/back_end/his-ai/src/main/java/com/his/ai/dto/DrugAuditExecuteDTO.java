package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方审核入参。
 * <p>
 * 只需要处方ID —— 患者信息、诊断、处方明细全部由服务端按处方ID回查，
 * 一是避免前端把数据拼歪，二是杜绝「前端传什么就审什么」造成的绕过。
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
