package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者既往疾病史出参
 */
@Data
public class PatientPastDiseaseVO {
    /**
     * 既往疾病史ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;
    /**
     * 疾病名称
     */
    private String diseaseName;
    /**
     * 疾病编码（ICD编码）
     */
    private String diseaseCode;
    /**
     * 确诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diagnosisDate;
    /**
     * 确诊科室
     */
    private String diagnosisDept;
    /**
     * 治疗方案
     */
    private String treatmentPlan;
    /** 当前控制情况（已治愈/控制良好/未控制/随访中） */
    private String currentStatus;
    /**
     * 复发次数
     */
    private Integer relapseCount;
    /**
     * 最近随访日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastFollowupDate;
}
