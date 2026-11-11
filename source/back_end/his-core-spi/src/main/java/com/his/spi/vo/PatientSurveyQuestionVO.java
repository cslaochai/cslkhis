package com.his.spi.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 患者端-满意度问卷题目。
 */
@Data
public class PatientSurveyQuestionVO {

    /**
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 题号
     */
    private Integer seqNo;

    /**
     * 题干
     */
    private String title;

    /**
     * 题型（1-量表 2-单选 3-多选 4-NPS 5-开放文本）
     */
    private Integer questionType;

    /**
     * 是否必答（0-否 1-是）
     */
    private Integer required;

    /**
     * 满分（量表 5 / NPS 10；选项与文本题为空）
     */
    private Integer maxScore;

    /**
     * 评价维度码值（1-挂号便捷 … 7-总体印象）
     */
    private Integer dimension;
}
