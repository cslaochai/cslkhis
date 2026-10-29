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

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}
