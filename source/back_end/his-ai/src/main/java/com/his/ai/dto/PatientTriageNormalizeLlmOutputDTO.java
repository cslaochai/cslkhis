package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 导诊口语归一的模型输出结构（与 prompts/triage-normalize.md 的 JSON 契约对应）。
 *
 * <p><b>模型在这里没有"推荐权"：</b>它只产出症状词、检索文本和追问，
 * 科室推荐是 {@code biz_triage_rule} 的事。这个契约刻意不含 deptId / deptName 字段 ——
 * 没有字段就填不进去，比在提示词里写十遍"不许推荐科室"管用。
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
