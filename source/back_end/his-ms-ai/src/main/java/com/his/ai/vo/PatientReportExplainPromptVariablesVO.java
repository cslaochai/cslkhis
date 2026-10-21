package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者端检验报告白话解释提示词变量。
 */
@Data
public class PatientReportExplainPromptVariablesVO implements PromptVariables {

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 解读口径提示（由规则代码生成）
     */
    private String ruleSummary;

    /**
     * 本次报告的结果项明细
     */
    private String items;
}