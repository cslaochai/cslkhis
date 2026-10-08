package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 编码推荐入参。
 */
@Data
@Schema(description = "ICD-10 编码推荐入参")
public class Icd10PredictDTO {

    @Schema(description = "病历ID。传了则从病历取文本；与文本字段同时存在时以文本字段为准")
    private Long recordId;

    /**
     * 患者ID
     */
    @Schema(description = "患者ID，仅用于审计检索")
    private Long patientId;

    @Schema(description = "主诉")
    private String chiefComplaint;

    @Schema(description = "现病史")
    private String presentIllness;

    @Schema(description = "专科检查")
    private String specialistExam;

    /**
     * 诊断
     */
    @Schema(description = "诊断原文")
    private String diagnosis;

    @Schema(description = "候选集上限。不传则取配置 ai.retrieve-topn（默认 50）")
    private Integer topN;
}
