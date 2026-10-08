package com.his.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 检验结果解读能力的模型输出结构（与 prompts/lab-interpret.md 的 JSON 契约一一对应）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabInterpretLlmOutputDTO {

    /**
     * 趋势解读：同一患者多次结果的走向
     */
    private String trendSummary;

    /**
     * 检验结论 / 诊断草稿
     */
    private String conclusion;

    /**
     * 建议列表（复查、进一步检查、临床结合等）
     */
    private List<String> suggestions = new ArrayList<>();

    /**
     * 需要重点关注的项
     */
    private List<Item> items = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        /**
         * 项目名称，必须是输入里真实存在的项目
         */
        private String itemName;

        /**
         * 该项目的解读
         */
        private String interpretation;

        /**
         * 关注级别 1-提示 2-关注 3-需尽快处理。模型不允许给 3 以上的语义级别，
         * 危急值本身由硬规则判定，不走这里。
         */
        private Integer level;
    }
}
