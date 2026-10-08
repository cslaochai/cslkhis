package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满意度：维度均分（升序= 短板在前）。
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