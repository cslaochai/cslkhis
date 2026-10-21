package com.his.clinical.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.enums.QcRuleEnum;
import lombok.Data;

/**
 * 一条质控问题（一次规则命中）。
 */
@Data
public class QcIssueVO {

    /**
     * 数据库主键；内存态（执行前的预览）为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 所属质控单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long qcId;

    /** 质控单号 */
    private String qcNo;

    /** 病历来源（OUTPATIENT-门诊病历 INPATIENT-住院文书） */
    private String recordSource;

    /** 被质控病历ID（随来源指向门诊病历或住院文书） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 患者ID */
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

    public static QcIssueVO of(QcRuleEnum rule, String errorDetail, String evidence) {
        QcIssueVO issue = new QcIssueVO();
        issue.ruleCode = rule.getCode();
        issue.ruleName = rule.getName();
        issue.dimension = rule.getDimension().getCode();
        issue.dimensionText = rule.getDimension().getLabel();
        issue.severity = rule.getSeverity().getCode();
        issue.severityText = rule.getSeverity().getLabel();
        issue.deduct = rule.getSeverity().getDeduct();
        issue.fieldName = rule.getFieldName();
        issue.errorDetail = errorDetail;
        issue.suggestion = rule.getSuggestion();
        issue.evidence = evidence;
        issue.basis = rule.getBasis();
        return issue;
    }
}
