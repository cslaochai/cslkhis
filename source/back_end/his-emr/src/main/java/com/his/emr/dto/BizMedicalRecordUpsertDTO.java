package com.his.emr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病历新增/修改入参（用 recordId 是否为空区分新增/修改）
 */
@Data
public class BizMedicalRecordUpsertDTO {
    /**
     * 病历ID，新增时为空，修改时必填
     */
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

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
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 就诊类型（1-初诊 2-复诊）
     */
    private Integer visitType;

    /**
     * 科室ID
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 体温（℃）
     */
    private String temperature;

    /**
     * 脉搏（次/分）
     */
    private String pulse;

    /**
     * 呼吸（次/分）
     */
    private String respiration;

    /**
     * 收缩压（mmHg）
     */
    private String systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private String diastolicPressure;

    /**
     * 一般情况
     */
    private String generalCondition;

    /**
     * 皮肤黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部
     */
    private String headNeck;

    /**
     * 胸肺
     */
    private String chestLung;

    /**
     * 心脏
     */
    private String heart;

    /**
     * 腹部
     */
    private String abdomen;

    /**
     * 脊柱四肢
     */
    private String spineLimbs;

    /**
     * 神经系统
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断（主诊断+次诊断）
     */
    private String diagnosis;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 处理意见
     */
    private String treatmentPlan;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）
     */
    private Integer reviewStatus;

    /**
     * 审核人
     */
    private String reviewBy;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewRemark;

    /**
     * 患者引导单PDF路径
     */
    private String guidePdfPath;
}
