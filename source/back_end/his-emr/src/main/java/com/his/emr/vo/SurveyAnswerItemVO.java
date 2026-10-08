package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 满意度逐题答案（详情展示 + 统计的最小粒度）。
 */
@Data
public class SurveyAnswerItemVO implements Serializable {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long itemId;

    /**
     * 评价维度
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
     * 题型
     */
    private Integer questionType;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 选项文本
     */
    private String optionLabel;

    /**
     * 文本题回答
     */
    private String textValue;
}
