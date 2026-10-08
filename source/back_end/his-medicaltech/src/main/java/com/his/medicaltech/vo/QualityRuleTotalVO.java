package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单条规则的分母 / 命中（P5.3 的总览取数结果）。
 */
@Data
@Schema(description = "规则分母与命中数")
public class QualityRuleTotalVO {

    /** 规则编码 */
    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "检查总数（分母）")
    private Long checkedTotal;

    @Schema(description = "问题数（分子）")
    private Long hitTotal;
}
