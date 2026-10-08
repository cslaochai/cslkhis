package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 会诊应答（接诊）入参：会诊方确认"我来做这次会诊"。
 */
@Data
public class ConsultationAcceptDTO implements Serializable {

    /**
     * 会诊ID（必填）
     */
    @NotNull(message = "会诊ID不能为空")
    private Long consultationId;

    /**
     * 接诊说明（可空）
     */
    private String remark;
}
