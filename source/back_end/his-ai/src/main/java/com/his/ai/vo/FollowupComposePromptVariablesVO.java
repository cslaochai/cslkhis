package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 随访话术生成提示词变量。
 *
 * <p>对应 {@code prompts/followup-compose.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>只给「病种 + 随访类型」两个变量：话术里的医学内容一概不给，
 * 模型空间压到最小，避免它顺口写出用药建议。
 */
@Data
public class FollowupComposePromptVariablesVO implements PromptVariables {

    /**
     * 随访类型名（模板类型，不含医学内容）
     */
    private String followupTypeName;

    /**
     * 病种上下文（诊断 + 在管慢病病名，空时为「暂无慢病档案与诊断信息」）
     */
    private String diseaseContext;
}