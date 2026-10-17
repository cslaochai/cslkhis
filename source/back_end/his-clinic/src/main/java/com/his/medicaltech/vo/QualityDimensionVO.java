package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 一个维度的小结（P5.3）。
 */
@Data
@Schema(description = "数据质量维度小结")
public class QualityDimensionVO {

    /** 维度 */
    @Schema(description = "维度码")
    private String dimension;

    @Schema(description = "维度名称")
    private String dimensionText;

    @Schema(description = "维度含义")
    private String description;

    @Schema(description = "规则条数")
    private Integer ruleCount;

    @Schema(description = "规则分母合计")
    private Long checkedTotal;

    /** 问题数 */
    @Schema(description = "问题数合计")
    private Long issueCount;

    @Schema(description = "合规率 %（加权口径）")
    private BigDecimal passRate;

    @Schema(description = "命中规则条数")
    private Integer dirtyRuleCount;

    @Schema(description = "该维度下的规则明细")
    private List<QualityRuleVO> rules;
}
