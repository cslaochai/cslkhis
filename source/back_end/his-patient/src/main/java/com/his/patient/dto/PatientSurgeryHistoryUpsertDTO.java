package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 患者手术外伤史新增/修改入参
 */
@Data
public class PatientSurgeryHistoryUpsertDTO {
    /**
     * 手术外伤史ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 手术/外伤名称
     */
    @NotBlank(message = "手术名称不能为空")
    private String surgeryName;
    /**
     * 手术/外伤日期
     */
    @NotNull(message = "手术日期不能为空")
    private LocalDate surgeryDate;
    /**
     * 手术类型（如：门诊、住院）
     */
    private String surgeryType;
    /**
     * 主刀医生
     */
    private String surgeon;
    /**
     * 麻醉方式
     */
    private String anesthesiaType;
    /**
     * 手术医院
     */
    private String hospitalName;
    /**
     * 术后诊断
     */
    private String postopDiagnosis;
    /** 恢复情况（良好/一般/差/死亡） */
    private String recoveryStatus;
    /** 术后并发症 */
    private String complications;
}
