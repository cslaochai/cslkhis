package com.his.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识文档切块预览请求（只计算不落库）。
 */
@Data
public class KnowledgePreviewDTO {

    @NotBlank(message = "内容不能为空")
    private String content;
}
