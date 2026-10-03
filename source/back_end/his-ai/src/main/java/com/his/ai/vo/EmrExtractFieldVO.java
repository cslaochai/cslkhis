package com.his.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽取出的单个病历字段（候选值，需医生采纳后才进病历）。
 */
@Data
@Schema(description = "抽取出的病历字段")
public class EmrExtractFieldVO {

    @Schema(description = "字段 key，与 biz_medical_record 的字段名一致，前端据此写入表单")
    private String field;

    @Schema(description = "字段中文名")
    private String fieldLabel;

    @Schema(description = "抽取出的值")
    private String value;

    @Schema(description = "来源：HARD_RULE-按原文标签逐字切分（未改写），LLM-模型从自由文本中搬运")
    private String source;

    @Schema(description = "原文依据：HARD_RULE 为切分出的原文本身；LLM 为模型回引且已校验存在于原文中的片段")
    private String evidence;
}
