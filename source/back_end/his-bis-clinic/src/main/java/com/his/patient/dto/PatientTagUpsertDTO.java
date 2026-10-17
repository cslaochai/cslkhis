package com.his.patient.dto;

import com.his.common.validation.AllowedValues;
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
    @AllowedValues(values = {"1", "2"}, message = "标签来源取值不合法")
    private Integer sourceType;
}
