package com.his.patient.dto;

import lombok.Data;

import java.util.List;

/**
 * 患者批量打标签入参
 */
@Data
public class PatientTagBatchUpsertDTO {
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 标签列表
     */
    private List<PatientTagUpsertDTO> tagDTOs;
}
