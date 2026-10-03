package com.his.medicaltech.dto;

import lombok.Data;

/**
 * 执行检查入参
 */
@Data
public class InspectionExecuteDTO {
    /**
     * 检查记录ID
     */
    private Long recordId;

    /**
     * 执行人
     */
    private String executeBy;

    /**
     * 检查所见（结果描述）
     */
    private String resultDescription;

    /**
     * 检查结论
     */
    private String resultConclusion;

    /**
     * 建议
     */
    private String suggestions;
}
