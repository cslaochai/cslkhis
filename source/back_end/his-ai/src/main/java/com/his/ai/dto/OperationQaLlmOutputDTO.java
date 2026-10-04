package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 运营问数生成段的模型输出结构（与 prompts/operation-qa.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationQaLlmOutputDTO {

    /**
     * 对查询内容的简短概括
     */
    private String title;

    /**
     * 模型生成的 SELECT；问题与经营统计无关时为空串，执行前必须过安全闸门
     */
    private String sql;
}
