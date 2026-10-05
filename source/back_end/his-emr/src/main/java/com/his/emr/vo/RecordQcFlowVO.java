package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 三级质控流转单 VO
 */
@Data
public class RecordQcFlowVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 流转单号
     */
    private String flowNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历来源（OUTPATIENT/INPATIENT）
     */
    private String recordSource;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病历所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病历所属科室名称
     */
    private String deptName;

    /**
     * 流转状态（1-科级待审 2-病案室待审 3-医务处待审 4-终审通过 5-整改中）
     */
    private Integer flowStatus;

    /**
     * 当前停留级（1科级 2病案室 3医务处；整改中=需回到的级）
     */
    private Integer currentLevel;

    /**
     * 最近一次退回发生级
     */
    private Integer returnLevel;

    /**
     * 最近一次退回的缺陷明细
     */
    private String returnReason;

    /**
     * 最近一次退回的整改要求
     */
    private String returnRequirement;

    /**
     * 整改期限
     */
    private LocalDate returnDeadline;

    /**
     * 终审定级（1-甲级 2-乙级 3-丙级）
     */
    private Integer grade;

    /**
     * 终审评分
     */
    private Integer finalScore;

    /**
     * 终审意见
     */
    private String finalOpinion;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 备注
     */
    private String remark;

    // 文案（后端算好，前端零映射）
    private String flowStatusText;
    private String currentLevelText;
    private String returnLevelText;
    private String gradeText;
    private String recordSourceText;
}
