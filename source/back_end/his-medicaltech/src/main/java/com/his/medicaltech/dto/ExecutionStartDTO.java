package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 开始医技执行入参
 */
@Data
public class ExecutionStartDTO {

    /**
     * 执行记录ID
     */
    private Long id;

    /**
     * 执行人ID
     */
    private Long executorId;

    /**
     * 执行人姓名
     */
    private String executorName;

}
