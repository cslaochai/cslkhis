package com.his.clinical.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临床规则校验记录（临床规则校验记录）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_clinical_rule_check")
public class BizClinicalRuleCheck extends BaseEntity {

    /**
     * 校验编号
     */
    private String checkNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 规则类型（1-配伍禁忌 2-检验诊断关联性 3-用药合理性）
     */
    private Integer ruleType;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则内容
     */
    private String ruleContent;
    /**
     * 校验结果（0-不通过 1-通过）
     */
    private Integer checkResult;
    /**
     * 错误级别（1-警告 2-错误 3-严重）
     */
    private Integer errorLevel;
    /**
     * 错误详情
     */
    private String errorDetail;
    /**
     * 处理建议
     */
    private String suggestion;
    /**
     * 处理状态（1-待处理 2-已处理 3-已忽略）
     */
    private Integer checkStatus;
    /**
     * 校验人
     */
    private String checkBy;
    /**
     * 校验时间
     */
    private LocalDateTime checkTime;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
