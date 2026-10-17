package com.his.patient.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院病历文书（读侧契约，供病历质控等跨模块读取）。
 */
@Data
public class InpatientRecordBriefVO {

    private Long id;

    private String recordNo;

    private Long patientId;

    private String patientName;

    private Integer gender;

    private Integer age;

    private Long deptId;

    private String deptName;

    private Long doctorId;

    private String doctorName;

    private Integer recordType;

    private Integer recordStatus;

    private String recordTitle;

    private String chiefComplaint;

    private String presentIllness;

    private String pastHistory;

    private String allergyHistory;

    private String diagnosisName;

    private String diagnosisCode;

    private String treatmentPlan;

    private String courseNote;

    private BigDecimal temperature;

    private Integer pulse;

    private Integer respiration;

    private Integer systolicPressure;

    private Integer diastolicPressure;

    private LocalDateTime recordTime;

    private LocalDateTime submitTime;

    private LocalDateTime archiveTime;
}
