package com.his.ai.vo;

import com.his.ai.support.PromptVariables;
import lombok.Data;

/**
 * 知识库问答提示词变量。
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