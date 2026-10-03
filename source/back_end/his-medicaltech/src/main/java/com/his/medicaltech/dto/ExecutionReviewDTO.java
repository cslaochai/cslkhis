package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 审核医技执行入参
 */
@Data
public class ExecutionReviewDTO {

    /**
     * 执行记录ID
     */
    private Long id;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核人姓名
     */
    private String reviewerName;

}
