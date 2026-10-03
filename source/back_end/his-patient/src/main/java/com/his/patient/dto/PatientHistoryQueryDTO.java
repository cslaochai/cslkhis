package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 患者病史类查询入参（过敏史/既往疾病史/手术外伤史/家族史通用）
 */
@Data
public class PatientHistoryQueryDTO {

    /**
     * 患者ID
     */
    @NotNull(message = "患者信息不能为空")
    private Long patientId;
}
