package com.his.ai.dto;

import lombok.Data;

/**
 * 意图路由的模型输出契约。
 */
@Data
public class FlowIntentRouteLlmOutputDTO {

    /** 选中的流程编码，只能来自候选清单 */
    private String flowCode;

    /** 置信度 0~1 */
    private Double confidence;

    /** 一句话判定理由 */
    private String reason;
}
