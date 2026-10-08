package com.his.emr.vo;

import lombok.Data;

/**
 * 规则命中统计。
 */
@Data
public class QcRuleMetricVO {

    /**
     * 规则编码
     */
    private String ruleCode;

    private String ruleName;

    /**
     * 维度（1-完整性 2-规范性 3-逻辑性）
     */
    private Integer dimension;

    private String dimensionText;

    /**
     * 严重度（1-提示 2-重要 3-否决）
     */
    private Integer severity;

    private String severityText;

    /**
     * 单条扣分
     */
    private Integer deduct;

    /**
     * 适用范围说明
     */
    private String scopeText;

    /**
     * 规则依据
     */
    private String basis;

    /**
     * 命中次数（问题条数）
     */
    private Long hitCount;

    /**
     * 涉及质控单数
     */
    private Long qcCount;

    /**
     * 累计扣分
     */
    private Long deductTotal;

    /**
     * 是否从未命中（观测事实，不等于规则正常）
     */
    private Boolean empty;
}
