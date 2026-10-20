package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 患者端影像报告白话解释提示词变量。
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