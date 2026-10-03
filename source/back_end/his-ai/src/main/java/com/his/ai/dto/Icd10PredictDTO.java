package com.his.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * ICD-10 编码推荐入参。
 * <p>
 * 两种用法：
 * <ol>
 *   <li>传 recordId —— 服务端从病历表取文本（推荐，前端不用拼字段）</li>
 *   <li>不传 recordId、直接传文本 —— 供尚未保存草稿的病历实时推荐</li>
 * </ol>
 * 两者同时存在时以入参文本为准（医生可能刚改过还没保存）。
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
