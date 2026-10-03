package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ICD-10 推荐能力的模型输出结构（与 prompts/icd10-predict.md 的 JSON 契约一一对应）。
 * <p>
 * 单独建一个类而不是用 Map，是为了让「契约」在编译期可见：模板改了字段，这里也必须改。
 * 注意这只是「模型说了什么」，还不是最终返回给前端的结果 —— 必须再过一道
 * 候选集校验（见 Icd10Capability），否则模型编造的编码会直接流到医生面前。
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
