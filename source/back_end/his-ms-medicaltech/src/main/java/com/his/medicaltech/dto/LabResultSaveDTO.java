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

    /**
     * 检验结论/诊断
     */
    private String diagnosis;

    /**
     * 建议
     */
    private String suggestions;

    /**
     * 检验结果明细列表
     */
    private List<LabResultItemSaveDTO> results;

    /**
     * 签名 PIN（软签二次确认）：签名人本人输入，明文不入库
     */
    private String pin;
}
