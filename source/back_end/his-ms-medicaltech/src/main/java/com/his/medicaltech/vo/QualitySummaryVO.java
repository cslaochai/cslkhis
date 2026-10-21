package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据质量总览（P5.3）。
 */
@Data
@Schema(description = "数据质量总览")
public class QualitySummaryVO {

    /** 统计时间（本次取数时刻，yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "统计时间")
    private String generatedAt;

    /** 规则总数 */
    @Schema(description = "规则总数")
    private Integer ruleCount;

    /** 各规则检查总数（分母）合计 */
    @Schema(description = "规则分母合计")
    private Long checkedTotal;

    /** 问题数 */
    @Schema(description = "问题条数合计")
    private Long issueCount;

    /** 总体合规率 %（=（分母-问题数）/分母，按分母加权口径） */
    @Schema(description = "总体合规率 %（加权口径，见维度说明）")
    private BigDecimal passRate;

    /** 没查出问题的规则数 */
    @Schema(description = "无问题的规则数")
    private Integer cleanRuleCount;

    /** 查出问题的规则数 */
    @Schema(description = "有问题的规则数")
    private Integer dirtyRuleCount;

    /** 分母为 0 的规则数（>0 说明有规则没生效，必须查） */
    @Schema(description = "分母为 0 的规则数（>0 说明有规则没生效，必须查）")
    private Integer emptyRuleCount;

    /** 严重问题条数（严重度为 3-严重 的规则命中的问题数合计） */
    @Schema(description = "严重问题条数")
    private Long highIssueCount;

    /** 五个维度的小结（完整性/一致性/及时性/唯一性/有效性） */
    @Schema(description = "五个维度的小结")
    private List<QualityDimensionVO> dimensions;

    /** 问题最多的前 5 条规则（按问题数倒序，没有问题命中的规则不进榜） */
    @Schema(description = "问题最多的前 5 条规则（按问题数倒序）")
    private List<QualityRuleVO> topRules;
}
