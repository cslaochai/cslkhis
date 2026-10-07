package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 预问诊小结提示词变量。
 *
 * <p>对应 {@code prompts/previsit-summary.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>走轻量模型分流；问答明细是前端提交的 JSON 快照，
 * 解析失败时只丢 {@code answersText}，主症状与补充描述仍能兜住规则摘要。
 */
@Data
public class PrevisitSummaryPromptVariablesVO implements PromptVariables {

    /**
     * 主症状，未填时为「未填写」
     */
    private String mainSymptom;

    /**
     * 问答明细（label：value 逐行），无则「无」
     */
    private String answersText;

    /**
     * 补充描述，无则「无」
     */
    private String freeText;
}