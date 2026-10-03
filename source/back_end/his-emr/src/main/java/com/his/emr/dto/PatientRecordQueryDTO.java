package com.his.emr.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 病历分页查询入参
 */
@Data
public class PatientRecordQueryDTO {

    /**
     * 挂号ID
     */
    @NotNull(message = "挂号不能为空")
    private Long registId;

}