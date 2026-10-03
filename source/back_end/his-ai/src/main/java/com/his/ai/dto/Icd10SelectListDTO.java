package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 编码下拉检索入参（不产生模型调用，纯字典查询）。
 */
@Data
@Schema(description = "ICD-10 编码下拉检索入参")
public class Icd10SelectListDTO {

    /**
     * 关键字
     */
    @Schema(description = "检索关键词，匹配编码或名称；为空则按排序返回")
    private String keyword;

    @Schema(description = "返回条数上限，默认 20")
    private Integer limit;
}
