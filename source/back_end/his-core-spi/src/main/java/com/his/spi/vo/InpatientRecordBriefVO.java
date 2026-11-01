package com.his.spi.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院病历文书（读侧契约，供病历质控等跨模块读取）。
 */
@Data
public class InpatientRecordBriefVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 病历文书号
     */
    private String recordNo;

    /**
     * 患者ID
     */
    private Long patientId;

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
     * 科室ID
     */
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 书写医生ID（员工ID）
     */
    private Long doctorId;

    /**
     * 书写医生姓名
     */
    private String doctorName;

    /**
     * 文书类型（1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录 9-会诊记录 10-转科记录 11-输血记录）
     */
    private Integer recordType;

    /**
     * 文书状态（1-草稿 2-已提交 3-已归档）
     */
    private Integer recordStatus;

    /**
     * 电子签名状态（0-未签名 1-已签名）。未签名的已提交病历不具备法律效力，质控规则 F14 判此项
     */
    private Integer signStatus;

    /**
     * 文书标题
     */
    private String recordTitle;

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
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 诊断编码（ICD-10）
     */
    private String diagnosisCode;

    /**
     * 诊疗计划 / 处理意见
     */
    private String treatmentPlan;

    /**
     * 病程记录正文（仅病程类文书使用）
     */
    private String courseNote;

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 记录时间（病程记录=记录时刻；入院记录=入院时间）
     */
    private LocalDateTime recordTime;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 归档时间
     */
    private LocalDateTime archiveTime;

    /**
     * 入院时间（取住院登记 biz_admission.admit_time，用于时限性质控）
     */
    private LocalDateTime admissionTime;

    /**
     * 出院时间（未出院为空，用于出院记录时限与离院方式质控）
     */
    private LocalDateTime dischargeTime;

    /**
     * 病案首页主诊断名称（biz_inpatient_summary.main_diagnosis_name）
     */
    private String mainDiagnosisName;

    /**
     * 病案首页主诊断编码（biz_inpatient_summary.main_diagnosis_code）
     */
    private String mainDiagnosisCode;

    /**
     * 病案首页入院途径（病案首页必填项）
     */
    private Integer admitWay;

    /**
     * 病案首页离院方式（病案首页必填项，出院后才有值）
     */
    private Integer dischargeWay;
}
