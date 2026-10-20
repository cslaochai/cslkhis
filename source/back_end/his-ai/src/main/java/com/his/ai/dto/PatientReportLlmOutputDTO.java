package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 患者端报告解读的模型输出结构（与 prompts/patient-report-explain.md 的 JSON 契约对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientReportLlmOutputDTO {

    /**
     * 面向患者的一句话总览，不超过 80 字。
     * 只描述「有几项、几项不在范围内」，不下结论、不给处置。
     */
    private String summary;
}
