package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端导诊口语归一入参。
 */
@Data
@Schema(description = "患者端导诊口语归一入参")
public class PatientTriageNormalizeDTO {

    @NotBlank(message = "description不能为空")
    @Schema(description = "患者原话主诉", example = "这两天脑袋昏昏沉沉的还想吐")
    private String description;
}
