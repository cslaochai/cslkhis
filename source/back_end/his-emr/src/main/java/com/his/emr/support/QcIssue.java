package com.his.emr.support;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 一条质控问题（一次规则命中）。
 *
 * <p>直接落质控问题明细，同时作为接口出参 ——
 * 只有一个模型，就不会出现"落库的字段和返回的字段不一样"这种问题。
 */
@Data
public class QcIssue {

    /**
     * 数据库主键；内存态（执行前的预览）为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long qcId;

    private String qcNo;

    private String recordSource;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 维度（1-完整性 2-规范性 3-逻辑性）
     */
    private Integer dimension;

    /**
     * 维度中文
     */
    private String dimensionText;

    /**
     * 严重度（1-提示 2-重要 3-否决）
     */
    private Integer severity;

    /**
     * 严重度中文
     */
    private String severityText;

    /**
     * 扣分
     */
    private Integer deduct;

    /**
     * 问题字段
     */
    private String fieldName;

    /**
     * 问题描述
     */
    private String errorDetail;

    /**
     * 整改建议
     */
    private String suggestion;

    /**
     * 病历原文证据（截断），用于"指着原文说这里确实是空的"
     */
    private String evidence;

    /**
     * 规则依据
     */
    private String basis;

    public static QcIssue of(QcRule rule, String errorDetail, String evidence) {
        QcIssue issue = new QcIssue();
        issue.ruleCode = rule.getCode();
        issue.ruleName = rule.getName();
        issue.dimension = rule.getDimension().getCode();
        issue.dimensionText = rule.getDimension().getText();
        issue.severity = rule.getSeverity().getCode();
        issue.severityText = rule.getSeverity().getText();
        issue.deduct = rule.getSeverity().getDeduct();
        issue.fieldName = rule.getFieldName();
        issue.errorDetail = errorDetail;
        issue.suggestion = rule.getSuggestion();
        issue.evidence = evidence;
        issue.basis = rule.getBasis();
        return issue;
    }
}
