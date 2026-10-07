package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者端影像报告白话解释提示词变量。
 *
 * <p>对应 {@code prompts/patient-imaging-explain.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code hasDictIntro} 告诉模型词典里有没有该项目介绍：
 * 有则直接引用院内已审过的说法，没有才让它自己写，避免两个口径打架。
 */
@Data
public class PatientImagingExplainPromptVariablesVO implements PromptVariables {

    /**
     * 检查项目名称
     */
    private String itemName;

    /**
     * 检查方法
     */
    private String examMethod;

    /**
     * 阳性标志说明（取自字典 biz_common_positiveFlagEnum）
     */
    private String positiveText;

    /**
     * 影像词典是否已收录该项目介绍（true/false）
     */
    private String hasDictIntro;

    /**
     * 影像所见
     */
    private String findings;

    /**
     * 影像诊断结论
     */
    private String conclusions;

    /**
     * 检查建议
     */
    private String suggestions;
}