package com.his.emr.vo;

import lombok.Data;

/**
 * 规则命中统计。
 *
 * <p><b>必须列出全部规则，包括命中数为 0 的</b>。这是 P5.3 数据质量报表踩过的坑：
 * 只从明细表 GROUP BY 出来的清单，会把"这条规则从没命中过"和"这条规则根本没实现"
 * 显示成同一个样子。所以本 VO 由规则枚举**左连接**统计结果生成，
 * 并额外给出 {@code empty} 标记 —— 0 命中是观测值，不是"没问题"。
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
