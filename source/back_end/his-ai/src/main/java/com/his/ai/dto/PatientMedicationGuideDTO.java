package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者端用药说明入参。
 */
@Data
@Schema(description = "患者端用药说明入参")
public class PatientMedicationGuideDTO {

    @NotNull(message = "prescriptionId不能为空")
    @Schema(description = "处方ID", example = "2103793966972424200")
    private Long prescriptionId;
}
