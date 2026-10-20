package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 导诊口语归一的模型输出结构（与 prompts/triage-normalize.md 的 JSON 契约对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientTriageNormalizeLlmOutputDTO {

    /**
     * 归一后的标准症状词，顿号分隔
     */
    private String terms;

    /**
     * 拿去查规则表的检索文本
     */
    private String searchText;

    /**
     * 补充追问
     */
    private List<String> followUps;
}
