package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 处方审核能力的模型输出结构（与 prompts/drug-audit.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugAuditLlmOutputDTO {

    private List<Finding> findings = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Finding {

        private Integer errorLevel;

        private String category;

        private String errorDetail;

        private String suggestion;

        private String relatedDrugs;
    }
}
