package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查申请新增入参
 */
@Data
public class BizInspectionApplyUpsertDTO {
    /**
     * 申请单号（留空时由后端生成）
     */
    private String applyNo;

    /**
     * 患者ID
     */
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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 挂号ID
     */
    private Long registId;

    /**
     * 病历ID
     */
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 开单科室ID
     */
    private Long deptId;

    /**
     * 开单科室名称
     */
    private String deptName;

    /**
     * 开单医生ID
     */
    private Long doctorId;

    /**
     * 开单医生姓名
     */
    private String doctorName;

    /**
     * 检查项目ID
     */
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
     * 执行检查科室ID
     */
    private Long inspectionDeptId;

    /**
     * 执行检查科室名称
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
     * 病情摘要
     */
    private String diseaseSummary;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 是否急诊：0-否 1-是
     */
    private Integer isEmergency;

    /**
     * 项目价格，单位：元
     */
    private BigDecimal price;

    /**
     * 申请状态：1-待缴费 2-待检查 3-已检查 4-已取消
     */
    private Integer applyStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 预约检查时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;
}
