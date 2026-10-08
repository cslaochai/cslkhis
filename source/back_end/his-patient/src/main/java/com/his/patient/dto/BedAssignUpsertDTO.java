package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 安排床位入参（含跨科调配）
 */
@Data
public class BedAssignUpsertDTO {

    /** 来源等床记录ID */
    @NotNull(message = "排队记录不能为空")
    private Long waitId;

    /** 床位ID */
    @NotNull(message = "床位不能为空")
    private Long bedId;

    /** 备注 */
    private String remark;
}
