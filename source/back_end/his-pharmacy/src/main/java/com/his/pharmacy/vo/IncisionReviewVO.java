package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * I 类切口预防用药点评行
 */
@Data
public class IncisionReviewVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 点评编号 */
    private String reviewNo;

    /** 手术申请单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operationApplyId;

    /** 手术申请单号 */
    private String applyNo;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 手术科室 */
    private String deptName;

    /** 手术名称 */
    private String operationName;

    /** 手术编码 ICD-9-CM-3 */
    private String operationCode;

    /** 手术开始时间 */
    private LocalDateTime operationTime;

    /** 主刀医师 */
    private String surgeonName;

    /** 切口等级 */
    private Integer incisionLevel;

    /** 预防用药药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 预防用药名称 */
    private String drugName;

    /** 预防用药分级（快照：1/2/3） */
    private Integer antibioticLevel;

    private String antibioticLevelText;

    /** 是否有预防用药指征（0-无 1-有） */
    private Integer indicationFlag;

    /** 给药时机 */
    private Integer timingType;

    private String timingTypeText;

    /** 预防用药总时长 */
    private Integer courseHours;

    /** 是否联合用药（0-否 1-是） */
    private Integer comboFlag;

    /** 联合用药理由 */
    private String comboReason;

    /** 特殊使用级是否有抗菌药物管理工作组会诊同意（0-无 1-有） */
    private Integer consultFlag;

    /** 点评结论（1-合理 2-不合理） */
    private Integer reviewResult;

    private String reviewResultText;

    /** 问题码（逗号分隔 41~48） */
    private String problemTypes;

    private String problemTypesText;

    /** 点评意见 */
    private String reviewOpinion;

    /** 点评人员工ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;

    /** 点评人姓名 */
    private String reviewerName;

    /** 点评时间 */
    private LocalDateTime reviewTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 备注 */
    private String remark;
}
