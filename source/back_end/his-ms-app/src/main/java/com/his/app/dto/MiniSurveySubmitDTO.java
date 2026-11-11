package com.his.app.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 患者端-提交满意度问卷。
 */
@Data
public class MiniSurveySubmitDTO implements Serializable {

    /**
     * 就诊人ID
     */
    @NotNull(message = "就诊人不能为空")
    private Long patientId;

    /**
     * 发放单ID
     */
    @NotNull(message = "问卷不能为空")
    private Long dispatchId;

    /**
     * 是否匿名（0-否 1-是）
     */
    @AllowedValues(values = {"0", "1"}, message = "是否匿名取值不合法")
    private Integer anonymousFlag;

    /**
     * 开放意见
     */
    private String commentText;

    /**
     * 逐题作答
     */
    @Valid
    @NotEmpty(message = "答卷内容不能为空")
    private List<Answer> answers;

    /**
     * 逐题作答行。
     */
    @Data
    public static class Answer implements Serializable {
        /**
         * 题目ID
         */
        @NotNull(message = "题目不能为空")
        private Long itemId;
        /**
         * 得分（量表 1-5 / NPS 0-10）
         */
        private Integer score;
        /**
         * 选项文本（多选以「；」拼接）
         */
        private String optionLabel;
        /**
         * 文本题回答
         */
        private String textValue;
    }
}
