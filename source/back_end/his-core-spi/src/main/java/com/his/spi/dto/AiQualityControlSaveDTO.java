package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 病历质控结果写入入参：把一次内涵质控的结论落成一条待处理的质控单。
 */
@Data
@NoArgsConstructor
public class AiQualityControlSaveDTO {

    /**
     * 关联病历ID
     */
    private Long recordId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 质控类型（0-综合 1-完整性检查 2-规范性检查 3-逻辑性检查 4-AI内涵质控）
     */
    private Integer qcType;

    /**
     * 本次质控的口径说明
     */
    private String qcContent;

    /**
     * 质控结果（1-通过 0-不通过）
     */
    private Integer qcResult;

    /**
     * 问题条数
     */
    private Integer errorCount;

    /**
     * 问题明细
     */
    private String errorDetail;

    /**
     * 处理状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer qcStatus;

    /**
     * 备注（模型未参与时的口径说明）
     */
    private String remark;
}
