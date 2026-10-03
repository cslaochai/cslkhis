package com.his.patient.dto;

import lombok.Data;

/**
 * 患者标签入参
 */
@Data
public class PatientTagUpsertDTO {
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 标签ID
     */
    private Long tagId;
    /**
     * 标签来源：1-手动打标 2-系统自动打标
     */
    private Integer sourceType;
}
