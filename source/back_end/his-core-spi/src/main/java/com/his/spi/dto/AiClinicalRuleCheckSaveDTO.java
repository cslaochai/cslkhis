package com.his.spi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 临床规则核查结果写入入参：把一次用药审核的结论落成一条待处理的核查记录。
 */
@Data
@NoArgsConstructor
public class AiClinicalRuleCheckSaveDTO {

    /**
     * 关联病历ID（核查记录必须挂在病历上，为空时调用方不应发起写入）
     */
    private Long recordId;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）
     */
    private Integer ruleType;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 本次核查的口径说明
     */
    private String ruleContent;

    /**
     * 核查结果（1-通过 0-不通过）
     */
    private Integer checkResult;

    /**
     * 最高严重级别（1-提示 2-警告 3-严重）
     */
    private Integer errorLevel;

    /**
     * 问题明细
     */
    private String errorDetail;

    /**
     * 处理建议
     */
    private String suggestion;

    /**
     * 处理状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer checkStatus;
}
