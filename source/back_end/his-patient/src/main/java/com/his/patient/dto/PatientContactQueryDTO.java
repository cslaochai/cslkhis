package com.his.patient.dto;

import lombok.Data;

/**
 * 患者联系人查询入参
 */
@Data
public class PatientContactQueryDTO {

    /**
     * 患者ID
     */
    private Long patientId;
}
