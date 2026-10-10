package com.his.patient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 换床入参
 */
@Data
public class InpatientTransferDTO {

    /**
     * 入院ID（必填）
     */
    @NotNull(message = "入院ID不能为空")
    private Long admissionId;

    /**
     * 目标床位ID（必填，必须空闲且与原床位同科室）
     */
    @NotNull(message = "目标床位不能为空")
    private Long newBedId;

    /**
     * 换床原因
     */
    private String reason;
}
