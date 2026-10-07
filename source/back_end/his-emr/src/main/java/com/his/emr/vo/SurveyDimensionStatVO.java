package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满意度：维度均分（升序= 短板在前）。
 *
 * <p>用答卷上的**维度快照**而不是题目表的当前维度：模板改维度后历史卷必须还记在
 * 它当时的维度下，否则报表会把去年的分数挪到今天的维度上。
 */
@Data
public class SurveyDimensionStatVO implements Serializable {

    /**
     * 维度码值
     */
    private Integer dimension;

    /**
     * 作答条数
     */
    private Long cnt;

    /**
     * 五分制均分
     */
    private BigDecimal avgScore;
}