package com.his.patient.dto;

import lombok.Data;

/**
 * 患者标签删除入参
 */
@Data
public class PatientTagDelDTO {
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 标签ID
     */
    private Long tagId;
}
