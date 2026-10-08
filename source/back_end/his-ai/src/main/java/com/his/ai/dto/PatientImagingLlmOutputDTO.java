package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 患者端影像报告解读的模型输出结构（与 prompts/patient-imaging-explain.md 的 JSON 契约对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientImagingLlmOutputDTO {

    /**
     * 检查介绍的白话补充（仅当词典未收录该项目时采用）
     */
    private String examIntro;

    /**
     * 报告「描述」部分的白话串讲
     */
    private String findingsPlain;

    /**
     * 报告「结论」部分的白话串讲
     */
    private String conclusionsPlain;

    /**
     * 报告「建议」部分的白话串讲
     */
    private String advicePlain;
}
