package com.his.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * 知识库问答返回。
 */
@Data
public class KnowledgeAskVO {

    /**
     * 用户问题（回显）
     */
    private String question;

    /**
     * 回答内容（模型生成或降级时返回检索原文）
     */
    private String answer;

    /**
     * 引用来源
     */
    private List<KnowledgeSourceVO> sources;

    /**
     * 是否降级（模型未参与时 true，答案来自检索原文）
     */
    private boolean degraded;

    /**
     * 降级原因（模型参与时为空）
     */
    private String degradeReason;

    /**
     * 耗时（毫秒）
     */
    private long latencyMs;
}
