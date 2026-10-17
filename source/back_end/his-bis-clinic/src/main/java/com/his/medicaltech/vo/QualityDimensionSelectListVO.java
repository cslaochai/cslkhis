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

    @Schema(description = "维度码")
    private String code;

    @Schema(description = "维度名称")
    private String text;

    @Schema(description = "维度含义")
    private String description;

    @Schema(description = "该维度下的规则条数")
    private Integer ruleCount;
}
