package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端报告解读入参。
 */
@Data
@Schema(description = "患者端报告解读入参")
public class PatientReportExplainDTO {

    @NotNull(message = "reportId不能为空")
    @Schema(description = "报告ID", example = "1")
    private Long reportId;
}
