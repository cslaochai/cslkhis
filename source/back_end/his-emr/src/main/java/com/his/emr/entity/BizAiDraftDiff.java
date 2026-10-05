package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 病历草稿 AI 留痕（草稿 → 终稿差异）。
 *
 * <p>只增不改不删：每一行就是一次「AI 草稿被医生终审」的独立样本，
 * 是未来 SFT 微调的训练原料（docs/AI能力施工手册.md G-10 数据飞轮）。
 * 同一病历多次「填草稿 → 保存」各留一行，无唯一键。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ai_draft_diff")
public class BizAiDraftDiff extends BaseEntity {

    /** 病历ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 挂号ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 接诊科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 接诊科室名称 */
    private String deptName;

    /** 终审医生ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /** 终审医生姓名 */
    private String doctorName;

    /** AI草稿原文（截断2000字） */
    private String draftText;

    /** 医生终稿（截断2000字） */
    private String finalText;

    /** 差异分段JSON（0-相同 1-删 2-增） */
    private String diffJson;

    /** 是否修改（1-有修改 0-未修改） */
    private Integer changed;
}
