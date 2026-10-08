package com.his.emr.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 答卷回收（录入/重填）入参 —— 一次发放一张答卷。
 */
@Data
public class SurveyAnswerUpsertDTO implements Serializable {

    /**
     * 发放单ID（回收必须回到一次发放上）
     */
    @NotNull(message = "发放单不能为空")
    private Long dispatchId;

    /**
     * 填报方式:1-患者自填 2-随访员代填 3-现场扫码（默认 2）
     */
    private Integer fillSource;

    /**
     * 是否匿名（0-否 1-是）
     */
    private Integer anonymousFlag;

    /**
     * 开放意见（服务端截到 1000）
     */
    private String commentText;

    /**
     * 备注
     */
    private String remark;

    /**
     * 明细项集合
     */
    @Valid
    @NotEmpty(message = "答卷内容不能为空")
    private List<Item> items;

    /**
     * 逐题作答。
     */
    @Data
    public static class Item implements Serializable {

        @NotNull(message = "题目ID不能为空")
        private Long itemId;

        /**
         * 得分：量表 1-5，NPS 0-10；选项/文本题为 null
         */
        private Integer score;

        /**
         * 选项文本（单选/多选，多选前端用「；」拼接）
         */
        private String optionLabel;

        /**
         * 文本题回答
         */
        private String textValue;
    }
}
