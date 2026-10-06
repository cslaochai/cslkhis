package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 一个维度的小结（P5.3）。
 *
 * <p><b>口径说明（页面上必须写出来，否则数字会被误读）</b>：
 * 维度合规率是把该维度下所有规则的分母与分子分别求和后计算的
 * （{@code (Σchecked - Σissue) / Σchecked}），属于加权口径。
 * 不同规则的分母不是同一批记录（有的是患者、有的是收费单、有的是检验结果行），
 * 所以这个数字只用于<b>横向比较五个维度的相对好坏与纵向看趋势</b>，
 * 不能理解为"全库数据有 92% 是干净的"。
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
