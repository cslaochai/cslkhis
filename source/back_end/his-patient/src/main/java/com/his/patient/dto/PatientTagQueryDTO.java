package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者标签查询入参
 */
@Data
public class PatientTagQueryDTO {
    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;
}
