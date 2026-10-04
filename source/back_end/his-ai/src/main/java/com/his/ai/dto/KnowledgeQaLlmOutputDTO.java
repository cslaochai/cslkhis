package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 知识库问答的模型输出（强类型，字段与提示词 JSON 一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KnowledgeQaLlmOutputDTO {

    /**
     * 基于参考材料生成的回答
     */
    private String answer;
}
