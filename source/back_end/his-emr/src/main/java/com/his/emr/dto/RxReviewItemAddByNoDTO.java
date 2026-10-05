package com.his.emr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按处方号补录点评明细入参
 */
@Data
public class RxReviewItemAddByNoDTO {

    /**
     * 批次ID
     */
    @NotNull(message = "批次不能为空")
    private Long batchId;

    /**
     * 处方号（快照）
     */
    @NotBlank(message = "处方号不能为空")
    private String prescriptionNo;
}
