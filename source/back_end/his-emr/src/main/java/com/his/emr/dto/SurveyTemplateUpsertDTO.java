package com.his.emr.dto;

import com.his.common.validation.InEnum;
import com.his.emr.enums.SurveyDimensionEnum;
import com.his.emr.enums.SurveyQuestionTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 问卷模板新增/修改入参（整卷覆盖：题目全删再插）。
 *
 * <p>题目按「整卷」提交而不是逐题 upsert：卷内题号唯一（uk_survey_item），
 * 逐题改会出现「先把 2 号改成 3 号、再把 3 号改成 4 号」这种中间态撞键。
 */
@Data
public class SurveyTemplateUpsertDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 问卷名称
     */
    @NotBlank(message = "问卷名称不能为空")
    private String templateName;

    /**
     * 适用场景（1-出院随访 2-门诊 3-住院在院 4-体检）
     */
    @NotNull(message = "适用场景不能为空")
    private Integer scene;

    /**
     * 状态:1-启用 2-停用（默认启用）
     */
    private Integer status;

    /**
     * 说明（调查目的、口径、上报去向）
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 明细项集合
     */
    @Valid
    @NotEmpty(message = "问卷至少要有 1 道题")
    private List<Item> items;

    /**
     * 题目（seqNo 由前端按顺序给，服务端不重排 —— 重排会让历史答卷的题号快照对不上号）。
     */
    @Data
    public static class Item implements Serializable {

        @NotNull(message = "题号不能为空")
        private Integer seqNo;

        /**
         * 维度（1-挂号便捷 2-医生服务 3-护士服务 4-环境与流程 5-费用透明 6-疗效与安全感 7-总体印象）
         */
        @NotNull(message = "评价维度不能为空")
        @InEnum(value = SurveyDimensionEnum.class,
                message = "评价维度取值不合法（1-挂号便捷 2-医生服务 3-护士服务 4-环境与流程 5-费用透明 6-疗效与安全感 7-总体印象）")
        private Integer dimension;

        /**
         * 题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）
         */
        @NotNull(message = "题型不能为空")
        @InEnum(value = SurveyQuestionTypeEnum.class,
                message = "题型取值不合法（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）")
        private Integer questionType;

        @NotBlank(message = "题干不能为空")
        private String title;

        /**
         * 是否必答:0-否 1-是（默认必答）
         */
        private Integer required;

        /**
         * 权重（不传按 1.00；0 表示不计分）
         */
        private BigDecimal weight;

        /**
         * 满分（量表 5 / NPS 10，不传按题型默认）
         */
        private Integer maxScore;
    }
}
