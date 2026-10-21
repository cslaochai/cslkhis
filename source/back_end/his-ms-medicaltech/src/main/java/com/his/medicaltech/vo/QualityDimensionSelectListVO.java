package com.his.medicaltech.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 维度字典项（前端做筛选下拉与维度含义提示）。
 */
@Data
@Schema(description = "数据质量维度字典项")
public class QualityDimensionSelectListVO implements Serializable {

    /** 维度码（枚举名，如 COMPLETENESS） */
    @Schema(description = "维度码")
    private String code;

    /** 维度名称 */
    @Schema(description = "维度名称")
    private String text;

    /** 维度含义（这类维度查的是哪一类问题） */
    @Schema(description = "维度含义")
    private String description;

    /** 该维度下的规则条数 */
    @Schema(description = "该维度下的规则条数")
    private Integer ruleCount;
}
