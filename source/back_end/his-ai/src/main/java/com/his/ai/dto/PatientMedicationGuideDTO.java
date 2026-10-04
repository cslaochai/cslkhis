package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 患者端用药说明入参。
 *
 * <p>同 {@link PatientReportExplainDTO} 的理由：只收处方ID，患者身份服务端取。
 * 一旦允许前端传 patientId，改个数字就能看别人的处方。
 */
@Data
@Schema(description = "患者端用药说明入参")
public class PatientMedicationGuideDTO {

    @NotBlank(message = "prescriptionId不能为空")
    @Schema(description = "处方ID", example = "2103793966972424200")
    private String prescriptionId;
}
