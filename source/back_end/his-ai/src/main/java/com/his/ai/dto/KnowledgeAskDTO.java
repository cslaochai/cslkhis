package com.his.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库问答请求。
 */
@Data
public class KnowledgeAskDTO {

    /**
     * 用户问题（必填）
     */
    @NotBlank(message = "问题不能为空")
    private String question;
}
