package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 处方条件查询入参
 */
@Data
public class PrescriptionQueryDTO {
    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;
    /**
     * 患者ID
     */
    @NotNull(message = "挂号不能为空")
    private Long registId;
}
