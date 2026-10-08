package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历草拟的模型输出结构（与 prompts/emr-draft.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmrDraftLlmOutputDTO {

    /**
     * 现病史草稿
     */
    private String presentIllness;

    /**
     * 还缺哪些要素才够写一份规范现病史（给医生的待办，不是病历正文）
     */
    private List<String> missingPoints = new ArrayList<>();

    /**
     * 这份草稿的依据与局限
     */
    private String summary;
}
