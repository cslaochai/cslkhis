package com.his.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 单条规则的分母 / 命中（P5.3 的总览取数结果）。
 *
 * <p>之所以把 checked 与 hit 放在同一条 SQL 里返回：两者必须来自同一次快照。
 * 分两次查会出现"分母是 10 分钟前的、分子是现在的"，得出来的合规率没有意义。
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
