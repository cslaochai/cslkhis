package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者分诊描述归一提示词变量。
 *
 * <p>对应 {@code prompts/triage-normalize.md} 的全部占位符（模板里只有{{description}} 一个变量）。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>本能力走轻量模型分流：只做「口语→规范症状词」的归一，
 * 检索文本仍以患者原话为主（见 buildSearchText），归一词只是增益。
 */
@Data
public class PatientTriageNormalizePromptVariablesVO implements PromptVariables {

    /**
     * 患者原话描述
     */
    private String description;
}