package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 满意度问卷题目（sql/164）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_survey_item")
public class BizSurveyItem extends BaseEntity {

    /**
     * 模板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 题号（卷内唯一）
     */
    private Integer seqNo;

    /**
     * 评价维度:1-挂号便捷 2-医生服务 3-护士服务 4-环境与流程 5-费用透明 6-疗效与安全感 7-总体印象
     */
    private Integer dimension;

    /**
     * 题型（1-量表 2-单选 3-多选 4-NPS推荐度 5-开放文本）
     */
    private Integer questionType;

    /**
     * 题干
     */
    private String title;

    /**
     * 是否必答（0-否 1-是）
     */
    private Integer required;

    /**
     * 权重（0 表示不计分）
     */
    private BigDecimal weight;

    /**
     * 满分：量表 5，NPS 10
     */
    private Integer maxScore;
}
