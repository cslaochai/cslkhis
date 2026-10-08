package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病历草稿 AI 留痕（草稿 → 终稿差异）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ai_draft_diff")
public class BizAiDraftDiff extends BaseEntity {

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 接诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 接诊科室名称
     */
    private String deptName;

    /**
     * 终审医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 终审医生姓名
     */
    private String doctorName;

    /**
     * AI草稿原文（截断2000字）
     */
    private String draftText;

    /**
     * 医生终稿（截断2000字）
     */
    private String finalText;

    /**
     * 差异分段JSON（0-相同 1-删 2-增）
     */
    private String diffJson;

    /**
     * 是否修改（1-有修改 0-未修改）
     */
    private Integer changed;
}
