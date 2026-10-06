package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据质量总览（P5.3）。
 *
 * <p>刻意同时给出 {@code issueCount}（问题条数）与 {@code dirtyRuleCount}（命中规则数）：
 * 前者说明"要改多少条数据"，后者说明"有几个环节出了问题"。
 * 实际整改时这两个数决定完全不同的动作 —— 1000 条同类问题改脚本，
 * 3 条不同类问题要分别查根因。
 */
@Data
@Schema(description = "数据质量总览")
public class QualitySummaryVO {

    @Schema(description = "统计时间")
    private String generatedAt;

    @Schema(description = "规则总数")
    private Integer ruleCount;

    @Schema(description = "规则分母合计")
    private Long checkedTotal;

    /** 问题数 */
    @Schema(description = "问题条数合计")
    private Long issueCount;

    @Schema(description = "总体合规率 %（加权口径，见维度说明）")
    private BigDecimal passRate;

    @Schema(description = "无问题的规则数")
    private Integer cleanRuleCount;

    @Schema(description = "有问题的规则数")
    private Integer dirtyRuleCount;

    @Schema(description = "分母为 0 的规则数（>0 说明有规则没生效，必须查）")
    private Integer emptyRuleCount;

    @Schema(description = "严重问题条数")
    private Long highIssueCount;

    @Schema(description = "五个维度的小结")
    private List<QualityDimensionVO> dimensions;

    @Schema(description = "问题最多的前 5 条规则（按问题数倒序）")
    private List<QualityRuleVO> topRules;
}
