package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 处方审核能力的模型输出结构（与 prompts/drug-audit.md 的 JSON 契约一一对应）。
 * <p>
 * 模板里明确禁止模型给出 errorLevel = 3 —— 3 代表硬规则拦截级别。
 * 即便如此，能力层仍会强制把模型返回的 3 压成 2（见 DrugAuditCapability），
 * 因为提示词是「约束」不是「保证」。
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
