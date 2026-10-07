package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 知识库问答提示词变量。
 *
 * <p>对应 {@code prompts/knowledge-qa.md} 的全部占位符。
 * 字段名与模板里的 {{占位符}} 一一对应。
 *
 * <p>{@code context} 是向量检索命中的原文片段。
 * 模型不可用时降级为直接回显这些片段（确定性输出），所以这段文本必须自足。
 */
@Data
public class KnowledgeQaPromptVariablesVO implements PromptVariables {

    /**
     * 用户问题
     */
    private String question;

    /**
     * 检索命中的知识库原文片段
     */
    private String context;
}