package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者健康档案聚合出参（六组一次带回）。
 */
@Data
@Schema(description = "患者健康档案（六组）")
public class PatientHealthProfileVO {

    /**
     * 患者ID
     */
    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    @Schema(description = "患者号")
    private String patientNo;

    /**
     * 患者姓名
     */
    @Schema(description = "患者姓名")
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    @Schema(description = "性别：1-男 2-女 9-未知")
    private Integer gender;

    /** 出生日期 */
    @Schema(description = "出生日期")
    private LocalDate birthDate;

    /**
     * 年龄
     */
    @Schema(description = "年龄（岁）")
    private Integer age;

    /** 过敏史明细 */
    @Schema(description = "过敏史明细")
    private List<PatientAllergyVO> allergies;

    /** 既往疾病史明细 */
    @Schema(description = "既往疾病史明细")
    private List<PatientPastDiseaseVO> pastDiseases;

    /** 手术外伤史明细 */
    @Schema(description = "手术外伤史明细")
    private List<PatientSurgeryHistoryVO> surgeryHistories;

    /** 家族史明细 */
    @Schema(description = "家族史明细")
    private List<PatientFamilyHistoryVO> familyHistories;

    /** 既往用药史明细 */
    @Schema(description = "既往用药史明细")
    private List<PatientMedicationHistoryVO> medications;

    /** 联系人明细 */
    @Schema(description = "联系人明细")
    private List<PatientContactVO> contacts;

    /** 主档过敏史文本（结构化明细的投影） */
    @Schema(description = "主档过敏史文本（结构化明细的投影）")
    private String allergyHistoryText;

    /** 主档既往病史文本（结构化明细的投影） */
    @Schema(description = "主档既往病史文本（结构化明细的投影）")
    private String medicalHistoryText;

    /** 主档联系人姓名（主要联系人的投影） */
    @Schema(description = "主档联系人姓名（主要联系人的投影）")
    private String contactNameText;

    /** 主档联系人电话（主要联系人的投影） */
    @Schema(description = "主档联系人电话（主要联系人的投影）")
    private String contactPhoneText;

    /** 主档与患者关系（主要联系人的投影，存的是文案不是码值） */
    @Schema(description = "主档与患者关系（主要联系人的投影，存的是文案不是码值）")
    private String contactRelationText;

    /** 过敏史只有文本没有明细（页面上不可维护） */
    @Schema(description = "过敏史只有文本没有明细（页面上不可维护）")
    private Boolean allergyTextOnly;

    /** 既往病史只有文本没有明细（页面上不可维护） */
    @Schema(description = "既往病史只有文本没有明细（页面上不可维护）")
    private Boolean pastDiseaseTextOnly;

    /** 联系人只有文本没有明细（页面上不可维护） */
    @Schema(description = "联系人只有文本没有明细（页面上不可维护）")
    private Boolean contactTextOnly;
}
