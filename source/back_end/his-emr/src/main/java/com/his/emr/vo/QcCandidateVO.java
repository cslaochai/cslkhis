package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待质控病历候选（质控工作台的入口列表）。
 *
 * <p>它把门诊病历与住院文书归一成一行"可执行质控的病历"，
 * 并带上**最近一次质控**的结论 —— 质控员要看的是"这份病历上次质控什么时候做的、得了几分"，
 * 而不是一张纯病历清单。
 */
@Data
public class QcCandidateVO {

    /**
     * OUTPATIENT / INPATIENT
     */
    private String recordSource;

    private String recordSourceText;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

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

    private String recordTypeText;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

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

    private String lastQcNo;

    private LocalDateTime lastQcTime;

    private Integer lastScore;

    /**
     * 最近一次质控的最高严重度（用于换算等级）
     */
    private Integer lastSeverityMax;

    private String lastGrade;

    private Integer lastResult;

    /**
     * 是否已质控
     */
    private Boolean qced;
}
