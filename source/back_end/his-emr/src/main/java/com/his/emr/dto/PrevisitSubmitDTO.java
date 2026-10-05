package com.his.emr.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 预问诊问卷提交入参（患者端）
 */
@Data
public class PrevisitSubmitDTO {

    /**
     * 挂号ID
     */
    @NotNull(message = "挂号ID不能为空")
    private Long registId;

    /**
     * 主症状
     */
    @NotBlank(message = "请选择主症状")
    private String mainSymptom;

    /**
     * 问答明细（通用问 + 主症状追问组）
     */
    @Valid
    private List<AnswerDTO> answers;

    /**
     * 患者补充描述（自由文本）
     */
    private String freeText;

    /**
     * 单条作答
     */
    @Data
    public static class AnswerDTO {

        /**
         * 题目标识（量表 Question.key）
         */
        @NotBlank(message = "题目标识不能为空")
        private String key;

        /**
         * 题目文本
         */
        @NotBlank(message = "题目文本不能为空")
        private String label;

        /**
         * 作答内容（选择题为选项文本，文本题为原文）
         */
        @NotBlank(message = "作答内容不能为空")
        private String value;
    }
}
