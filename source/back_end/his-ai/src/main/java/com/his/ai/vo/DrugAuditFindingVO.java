package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 处方审核问题条目。
 */
@Data
@Schema(description = "处方审核问题条目")
public class DrugAuditFindingVO {

    /** 判定来源（HARD_RULE-硬规则判定 LLM-模型判定） */
    @Schema(description = "来源：HARD_RULE-硬规则 LLM-模型")
    private String source;

    /** 错误级别（1-提示 2-警告 3-严重） */
    @Schema(description = "严重程度：1-提示 2-警告 3-严重（仅硬规则可给出 3）")
    private Integer errorLevel;

    /**
     * 类别
     */
    @Schema(description = "问题类别")
    private String category;

    /** 错误详情 */
    @Schema(description = "问题描述")
    private String errorDetail;

    /** 处理建议 */
    @Schema(description = "处理建议")
    private String suggestion;

    /** 涉及的药品名称 */
    @Schema(description = "涉及药品")
    private String relatedDrugs;

    /** 命中依据：硬规则给出的规则名与原文片段 */
    @Schema(description = "命中依据：硬规则命中时给出命中的规则名与原文依据")
    private String evidence;
}
