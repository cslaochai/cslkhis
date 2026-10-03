package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 下拉选项（字典查询，无模型调用）。
 */
@Data
@Schema(description = "ICD-10 下拉选项")
public class Icd10SelectListVO {

    /**
     * ICD编码
     */
    @Schema(description = "编码")
    private String icdCode;

    /**
     * 疾病名称
     */
    @Schema(description = "名称")
    private String icdName;

    /**
     * 分类
     */
    @Schema(description = "分类")
    private String icdCategory;
}
