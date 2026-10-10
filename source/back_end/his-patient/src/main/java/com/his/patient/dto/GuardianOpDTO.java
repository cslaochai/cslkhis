package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 就诊人绑定操作入参（解绑 / 设为默认），patientId 均指被操作的就诊人
 */
@Data
public class GuardianOpDTO implements Serializable {

    /**
     * 体检人ID
     */
    @NotNull(message = "就诊人ID不能为空")
    private Long patientId;
}
