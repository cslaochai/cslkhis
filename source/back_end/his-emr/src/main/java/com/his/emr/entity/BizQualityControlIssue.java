package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 病案质控问题明细实体（一条规则命中一行）。
 *
 * <p>为什么不把问题拼成字符串塞进质控检查记录.error_detail：
 * 病案室的真实问题是「这个月哪条规则扣分最多」「哪个科室问题最集中」，
 * 拼接串连 {@code GROUP BY} 都做不了。{@code error_detail} 保留为摘要，
 * 明细落在这里。
 */
@Data
@TableName("biz_quality_control_issue")
public class BizQualityControlIssue {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 质控单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long qcId;

    /**
     * 质控单号
     */
    private String qcNo;

    /**
     * 病历来源
     */
    private String recordSource;

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
     * 严重度（1-提示 2-重要 3-否决）
     */
    private Integer severity;
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
     * 病历原文证据（截断）
     */
    private String evidence;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
