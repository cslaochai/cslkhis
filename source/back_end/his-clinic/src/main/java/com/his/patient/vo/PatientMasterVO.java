package com.his.patient.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 患者主档（读侧契约）。
 */
@Data
public class PatientMasterVO {

    private Long id;

    private String patientNo;

    private String patientName;

    private Integer gender;

    private LocalDate birthDate;

    private Integer age;

    private String idCard;

    private String phone;

    private String address;

    private String bloodType;

    private Long masterId;

    private Integer mergeStatus;

    /**
     * 过敏史文本（与结构化过敏表并存）
     */
    private String allergyHistory;

    private String medicalInsuranceType;

    private String medicalInsuranceNo;

    private Integer delFlag;
}
