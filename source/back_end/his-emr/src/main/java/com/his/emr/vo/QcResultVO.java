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

    private String recordSource;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    private String recordNo;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    private String patientName;

    private String deptName;

    private Integer recordType;

    private String recordTypeText;

    private int issueCount;

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

    private List<QcIssueVO> issues = new ArrayList<>();
}
