package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用解释 · 按医保目录类别的分组汇总。
 */
@Data
@Schema(description = "费用解释-医保目录类别分组")
public class FeeCatalogGroupVO {

    /**
     * 0-自费 1-甲类 2-乙类 3-丙类
     */
    @Schema(description = "医保目录类别：0-自费 1-甲类 2-乙类 3-丙类")
    private Integer catalogType;

    @Schema(description = "类别文案")
    private String catalogText;

    @Schema(description = "该类别金额合计")
    private BigDecimal amount;

    @Schema(description = "占总费用比例（百分数，保留 1 位）")
    private BigDecimal ratio;

    @Schema(description = "这笔钱医保怎么算（规则文案，不含比例数字以外的承诺）")
    private String ruleText;

    @Schema(description = "该类别下的项目数")
    private Integer itemCount;

    @Schema(description = "该类别下的项目名（最多列 8 个，用于患者对照明细）")
    private List<String> itemNames;
}
