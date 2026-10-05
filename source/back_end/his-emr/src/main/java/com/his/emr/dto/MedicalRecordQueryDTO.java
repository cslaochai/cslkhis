package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病历分页查询入参
 */
@Data
public class MedicalRecordQueryDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;
}
