package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 检查申请单 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inspection_apply")
public class BizInspectionApply extends BaseEntity {
    /**
     * 申请单号
     */
    private String applyNo;

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
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 申请科室名称
     */
    private String deptName;

    /**
     * 申请医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 申请医生姓名
     */
    private String doctorName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 检查科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionDeptId;

    /**
     * 检查科室名称
     */
    private String inspectionDeptName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 病史摘要
     */
    private String diseaseSummary;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 是否急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 检查费用
     */
    private BigDecimal price;

    /**
     * 申请状态（1-已提交 2-已缴费 3-已预约 4-检查中 5-已出报告 6-已取消）
     */
    private Integer applyStatus;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 预约时间
     */
    private LocalDateTime appointmentTime;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）—— 开单即签，签名即锁定（改单须先作废签名）
     */
    private Integer signStatus;

    /**
     * 当前有效签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    /**
     * 最近一次签名时刻
     */
    private LocalDateTime signedTime;
}
