package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 推荐结果条目。
 * <p>
 * {@code icdName} 与 {@code icdCategory} 一律取自字典表，<b>不采用模型返回的名称</b>。
 * 模型只负责「选哪个编码」，不负责「编码叫什么名字」—— 名称错一个字，
 * 医生就会对系统失去信任。
 */
@Data
@Schema(description = "ICD-10 推荐结果条目")
public class Icd10PredictItemVO {

    @Schema(description = "ICD-10 编码，保证存在于字典表")
    private String icdCode;

    @Schema(description = "编码名称，取自字典表")
    private String icdName;

    @Schema(description = "所属分类，取自字典表")
    private String icdCategory;

    @Schema(description = "置信度 0-100")
    private Integer confidence;

    @Schema(description = "推荐依据")
    private String reasoning;

    @Schema(description = "来源：LLM-模型推荐 RULE-规则降级")
    private String source;
}
