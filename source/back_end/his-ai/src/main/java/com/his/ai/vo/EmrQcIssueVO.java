package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 病历质控问题条目。
 */
@Data
@Schema(description = "病历质控问题条目")
public class EmrQcIssueVO {

    @Schema(description = "来源：HARD_RULE-必填项规则 LLM-模型")
    private String source;

    /**
     * 维度
     */
    @Schema(description = "质控维度：completeness-完整性 regularity-规范性 logic-逻辑性")
    private String dimension;

    @Schema(description = "严重程度：1-轻微 2-一般 3-严重")
    private Integer severity;

    @Schema(description = "涉及的病历字段")
    private String fieldName;

    @Schema(description = "问题描述")
    private String errorDetail;

    @Schema(description = "修改建议")
    private String suggestion;

    @Schema(description = "病历原文依据")
    private String evidence;
}
