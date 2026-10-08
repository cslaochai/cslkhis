package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ICD-10 推荐能力的模型输出结构（与 prompts/icd10-predict.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Icd10LlmOutputDTO {

    private List<Prediction> predictions = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prediction {

        private String icdCode;

        private String icdName;

        private Integer confidence;

        private String reasoning;
    }
}
