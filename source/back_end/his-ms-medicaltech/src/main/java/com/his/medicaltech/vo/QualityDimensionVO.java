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

    /** 维度名称文案（由枚举 label 渲染） */
    @Schema(description = "维度名称")
    private String dimensionText;

    /** 维度含义（这类维度查的是哪一类问题） */
    @Schema(description = "维度含义")
    private String description;

    /** 该维度下的规则条数 */
    @Schema(description = "规则条数")
    private Integer ruleCount;

    /** 该维度各规则检查总数（分母）合计 */
    @Schema(description = "规则分母合计")
    private Long checkedTotal;

    /** 问题数 */
    @Schema(description = "问题数合计")
    private Long issueCount;

    /** 合规率 %（=（分母-问题数）/分母，按分母加权口径） */
    @Schema(description = "合规率 %（加权口径）")
    private BigDecimal passRate;

    /** 该维度下查出问题的规则条数 */
    @Schema(description = "命中规则条数")
    private Integer dirtyRuleCount;

    /** 该维度下的规则明细 */
    @Schema(description = "该维度下的规则明细")
    private List<QualityRuleVO> rules;
}
