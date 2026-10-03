package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 病历内涵质控能力的模型输出结构（与 prompts/emr-qc.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmrQcLlmOutputDTO {

    private List<Issue> issues = new ArrayList<>();

    /**
     * 整体评价
     */
    private String summary;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {

        /**
         * completeness / regularity / logic
         */
        private String dimension;

        /**
         * 1-轻微 2-一般 3-严重
         */
        private Integer severity;

        private String fieldName;

        private String errorDetail;

        private String suggestion;

        /**
         * 病历原文片段，作为质控依据
         */
        private String evidence;
    }
}
