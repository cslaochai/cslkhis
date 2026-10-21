package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 运营问数结论段的模型输出结构（与 prompts/operation-qa-summary.md 的 JSON 契约对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationQaSummaryLlmOutputDTO {

    /**
     * 不超过 3 句的结论
     */
    private String summary;
}
