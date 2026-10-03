package com.his.medicaltech.dto;

import lombok.Data;

import java.util.List;

/**
 * 录入检验结果入参
 */
@Data
public class LabResultSaveDTO {
    /**
     * 检验记录ID
     */
    private Long recordId;

    /**
     * 检验执行人
     */
    private String executeBy;

    /** 检验结论/诊断 */
    private String diagnosis;

    /**
     * 建议
     */
    private String suggestions;

    /**
     * 检验结果明细列表
     */
    private List<LabResultItemSaveDTO> results;
}
