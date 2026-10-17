package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满意度：近 30 日趋势（按提交日聚合，只认有效卷）。
 */
@Data
public class SurveyDayTrendVO implements Serializable {

    /**
     * 提交日（yyyy-MM-dd）
     */
    private String statDate;

    /**
     * 有效卷数
     */
    private Long cnt;

    /**
     * 百分制均分
     */
    private BigDecimal avgScore100;
}