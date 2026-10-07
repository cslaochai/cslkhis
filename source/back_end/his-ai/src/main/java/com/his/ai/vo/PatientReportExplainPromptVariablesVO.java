package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者端检验报告白话解释提示词变量。
 *
 * <p>对应 {@code prompts/patient-report-explain.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code ruleSummary} 是代码算好的解读口径提示行，
 * 与影像解释一样遵循「事实层代码算、模型做解释」。
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