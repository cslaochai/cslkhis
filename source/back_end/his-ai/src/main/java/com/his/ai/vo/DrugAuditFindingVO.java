package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 处方审核问题条目。
 */
@Data
@Schema(description = "处方审核问题条目")
public class DrugAuditFindingVO {

    @Schema(description = "来源：HARD_RULE-硬规则 LLM-模型")
    private String source;

    @Schema(description = "严重程度：1-提示 2-警告 3-严重（仅硬规则可给出 3）")
    private Integer errorLevel;

    /**
     * 类别
     */
    @Schema(description = "问题类别")
    private String category;

    @Schema(description = "问题描述")
    private String errorDetail;

    @Schema(description = "处理建议")
    private String suggestion;

    @Schema(description = "涉及药品")
    private String relatedDrugs;

    @Schema(description = "命中依据：硬规则命中时给出命中的规则名与原文依据")
    private String evidence;
}
