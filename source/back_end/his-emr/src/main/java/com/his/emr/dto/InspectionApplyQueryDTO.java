package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 检查申请查询入参
 */
@Data
public class InspectionApplyQueryDTO {
    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /**
     * 挂号ID
     */
    @NotNull(message = "挂号不能为空")
    private Long registId;
}
