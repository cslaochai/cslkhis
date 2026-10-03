package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 患者健康档案聚合出参（六组一次带回）。
 *
 * <p>为什么聚合而不是让页面发六个请求：健康档案页要同时判断「自述文本与结构化明细是否分叉」，
 * 分叉的判据必须**在同一份快照上算**（文本来自主档、明细来自六张表）。分六个请求各自取数，
 * 用户点开页面时正好有人在改档案，就会看到「文本说明细为空、明细其实刚被加进来」这种自相矛盾的画面。
 *
 * <p>{@code xxxTextOnly} 三个标记的含义固定为一种：**明细为空、主档文本有值**。
 * 这表示这条档案只以自由文本的形式存在、在页面上无法增删改 —— 也就是「结构性缺失」。
 * 不拿「文本 ≠ 明细摘要」当分叉判据：医生手写的「青霉素过敏」与明细里的「青霉素（重度）」
 * 语义一致但字符串不同，按字符串比会把正常档案全部标成分叉，标记就失去意义了。
 */
@Data
@Schema(description = "患者健康档案（六组）")
public class PatientHealthProfileVO {

    /** 患者ID */
    @Schema(description = "患者ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号 */
    @Schema(description = "患者号")
    private String patientNo;

    /** 患者姓名 */
    @Schema(description = "患者姓名")
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    @Schema(description = "性别：1-男 2-女 3-未知")
    private Integer gender;

    @Schema(description = "出生日期")
    private LocalDate birthDate;

    /** 年龄 */
    @Schema(description = "年龄（岁）")
    private Integer age;

    @Schema(description = "过敏史明细")
    private List<PatientAllergyVO> allergies;

    @Schema(description = "既往疾病史明细")
    private List<PatientPastDiseaseVO> pastDiseases;

    @Schema(description = "手术外伤史明细")
    private List<PatientSurgeryHistoryVO> surgeryHistories;

    @Schema(description = "家族史明细")
    private List<PatientFamilyHistoryVO> familyHistories;

    @Schema(description = "既往用药史明细")
    private List<PatientMedicationHistoryVO> medications;

    @Schema(description = "联系人明细")
    private List<PatientContactVO> contacts;

    @Schema(description = "主档过敏史文本（结构化明细的投影）")
    private String allergyHistoryText;

    @Schema(description = "主档既往病史文本（结构化明细的投影）")
    private String medicalHistoryText;

    @Schema(description = "主档联系人姓名（主要联系人的投影）")
    private String contactNameText;

    @Schema(description = "主档联系人电话（主要联系人的投影）")
    private String contactPhoneText;

    @Schema(description = "主档与患者关系（主要联系人的投影，存的是文案不是码值）")
    private String contactRelationText;

    @Schema(description = "过敏史只有文本没有明细（页面上不可维护）")
    private Boolean allergyTextOnly;

    @Schema(description = "既往病史只有文本没有明细（页面上不可维护）")
    private Boolean pastDiseaseTextOnly;

    @Schema(description = "联系人只有文本没有明细（页面上不可维护）")
    private Boolean contactTextOnly;
}
