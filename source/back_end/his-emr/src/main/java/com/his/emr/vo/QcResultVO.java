package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次质控的结论。
 */
@Data
public class QcResultVO {

    /** 病历来源（OUTPATIENT-门诊病历 INPATIENT-住院文书） */
    private String recordSource;

    /** 被质控病历ID（随来源指向门诊病历或住院文书） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 病历号 */
    private String recordNo;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 病历归属科室名称 */
    private String deptName;

    /** 住院文书类型；门诊病历为 null */
    private Integer recordType;

    /** 文书类型文案（由枚举 label 渲染） */
    private String recordTypeText;

    /** 本次命中问题条数 */
    private int issueCount;

    /** 质控得分（100 分制，按问题严重度扣分） */
    private int score;

    /**
     * 最高严重度；无问题时为 0
     */
    private int severityMax;

    /**
     * 否决项命中条数
     */
    private int vetoCount;

    /**
     * 是否通过：命中任何严重度 ≥2 的问题即不通过
     */
    private boolean pass;

    /**
     * 病历质量等级：甲 / 乙 / 丙
     */
    private String grade;

    /**
     * 一句话结论
     */
    private String summary;

    /**
     * 本次实际执行的维度（qcType 为空时是三个维度全跑）
     */
    private List<Integer> dimensions = new ArrayList<>();

    /** 本次命中的问题明细（含规则依据与原文证据） */
    private List<QcIssueVO> issues = new ArrayList<>();
}
