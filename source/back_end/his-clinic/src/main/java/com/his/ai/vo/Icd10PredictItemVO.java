package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 推荐结果条目。
 */
@Data
@Schema(description = "ICD-10 推荐结果条目")
public class Icd10PredictItemVO {

    /** ICD-10 编码（已核对存在于编码字典） */
    @Schema(description = "ICD-10 编码，保证存在于字典表")
    private String icdCode;

    /** 编码名称（取自编码字典） */
    @Schema(description = "编码名称，取自字典表")
    private String icdName;

    /** 分类（编码所属目录分类，取自编码字典） */
    @Schema(description = "所属分类，取自字典表")
    private String icdCategory;

    /** 推荐置信度（0-100） */
    @Schema(description = "置信度 0-100")
    private Integer confidence;

    /** 判断依据 */
    @Schema(description = "推荐依据")
    private String reasoning;

    /** 推荐来源（LLM-模型推荐 RULE-规则降级） */
    @Schema(description = "来源：LLM-模型推荐 RULE-规则降级")
    private String source;
}
