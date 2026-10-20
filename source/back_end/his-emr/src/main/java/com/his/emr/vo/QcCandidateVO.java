package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待质控病历候选（质控工作台的入口列表）。
 */
@Data
public class QcCandidateVO {

    /**
     * 病历来源（OUTPATIENT-门诊病历 INPATIENT-住院文书）
     */
    private String recordSource;

    /** 病历来源文案（由枚举 label 渲染） */
    private String recordSourceText;

    /** 病历ID（随来源指向门诊病历或住院文书） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 病历号 */
    private String recordNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女，按后端枚举）
     */
    private Integer gender;

    /** 性别文案（由枚举 label 渲染） */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 住院文书类型；门诊为 null
     */
    private Integer recordType;

    /** 文书类型文案（由枚举 label 渲染） */
    private String recordTypeText;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    /** 文书状态文案（由枚举 label 渲染） */
    private String recordStatusText;

    /**
     * 病历时间（门诊=就诊日期，住院=记录时间）
     */
    private LocalDateTime recordTime;

    /**
     * 最近一次质控单ID；从未质控为 null
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastQcId;

    /** 最近一次质控编号；从未质控为 null */
    private String lastQcNo;

    /** 最近一次质控时间 */
    private LocalDateTime lastQcTime;

    /** 最近一次质控得分（100 分制；旧版质控未评分为 null） */
    private Integer lastScore;

    /**
     * 最近一次质控的最高严重度（用于换算等级）
     */
    private Integer lastSeverityMax;

    /** 最近一次质控等级（甲/乙/丙，命中否决项必为丙；无得分时为 null） */
    private String lastGrade;

    /** 最近一次质控结果（0-不通过 1-通过） */
    private Integer lastResult;

    /**
     * 是否已质控
     */
    private Boolean qced;
}
