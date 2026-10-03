package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者既往疾病史新增/修改入参
 */
@Data
public class PatientPastDiseaseUpsertDTO {
    /**
     * 既往疾病史ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 疾病名称
     */
    @NotBlank(message = "疾病名称不能为空")
    private String diseaseName;
    /**
     * 疾病编码（ICD编码）
     */
    private String diseaseCode;
    /**
     * 确诊日期
     */
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
    private LocalDate lastFollowupDate;
}
