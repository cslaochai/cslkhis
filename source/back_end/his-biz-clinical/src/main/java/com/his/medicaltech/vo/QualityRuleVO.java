package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单条规则的检查结果（P5.3）。
 */
@Data
@Schema(description = "数据质量规则执行结果")
public class QualityRuleVO {

    /** 规则编码 */
    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    /** 维度 */
    @Schema(description = "维度码")
    private String dimension;

    @Schema(description = "维度名称")
    private String dimensionText;

    @Schema(description = "严重度（1-提示 2-警告 3-严重）")
    private Integer severity;

    @Schema(description = "严重度文案")
    private String severityText;

    @Schema(description = "检查对象表")
    private String tableName;

    @Schema(description = "分母说明（在什么范围里查）")
    private String checkedDesc;

    @Schema(description = "整改建议")
    private String suggestion;

    @Schema(description = "依据")
    private String basis;

    @Schema(description = "检查总数（分母）")
    private Long checkedTotal;

    /** 问题数 */
    @Schema(description = "问题数（分子）")
    private Long issueCount;

    @Schema(description = "合规率 %（= (分母-问题数)/分母，一位小数）")
    private BigDecimal passRate;

    @Schema(description = "分母为 0（规则未生效，前端口径上要显式告警）")
    private Boolean empty;

    @Schema(description = "本次检查是否有问题")
    private Boolean hasIssue;
}
