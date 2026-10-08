package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 满意度逐题答案（sql/164）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_answer_item")
public class BizSurveyAnswerItem extends BaseEntity {

    /**
     * 答卷ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long answerId;

    /**
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 模板ID（冗余，按模板聚合时免去 join）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 评价维度（题目快照）
     */
    private Integer dimension;

    /**
     * 题号
     */
    private Integer seqNo;

    /**
     * 题干
     */
    private String title;

    /**
     * 题型:1-量表 2-单选 3-多选 4-NPS 5-开放文本
     */
    private Integer questionType;

    /**
     * 得分：量表 1-5，NPS 0-10；单选/多选/文本为 null
     */
    private Integer score;

    /**
     * 选项文本（单选/多选，多选取分号拼接）
     */
    private String optionLabel;

    /**
     * 文本题回答
     */
    private String textValue;
}
