package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开住院证入参（门诊医生站调用）
 *
 * <p>患者信息由前端**快照**传入而不是后端反查：门诊医生站屏幕上就有这些值，
 * 传过来既省一次跨模块查询，也符合"证面写的是什么就是什么"的业务语义。
 *
 * <p>{@code registId} 是本字段组里唯一真正重要的外键——它是"这张证是哪次门诊开的"的凭据。
 */
@Data
public class AdmissionOrderUpsertDTO {

    // 来源门诊线索

    /** 来源挂号ID（挂号信息的ID，原挂号单；门诊开证必传） */
    private Long registId;

    /** 来源挂号号（快照） */
    private String registNo;

    /** 来源就诊次ID（就诊次的就诊ID，可为空） */
    private Long visitId;

    // 患者快照

    /** 患者ID（必填） */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    private Integer gender;

    /** 年龄（快照） */
    private Integer age;

    /** 联系电话（快照） */
    private String phone;

    /** 身份证号（快照） */
    private String idCard;

    // 开证方

    /** 开证科室ID */
    private Long sourceDeptId;

    /** 开证科室名称（快照） */
    private String sourceDeptName;

    /** 开证医生ID */
    private Long sourceDoctorId;

    /** 开证医生姓名（快照） */
    private String sourceDoctorName;

    // 拟收治

    /** 拟收治科室ID（必填——没有收治目标的证，入院处无从排床） */
    @NotNull(message = "拟收治科室不能为空（没有收治目标的证，入院处无从排床）")
    private Long applyDeptId;

    /** 拟收治科室名称（快照） */
    private String applyDeptName;

    /** 拟诊ICD编码 */
    private String diagnosisCode;

    /** 拟诊名称 */
    private String diagnosisName;

    /** 病情与收治说明 */
    private String diagnosisNote;

    // 医保

    /** 医保类型 */
    private String insuranceType;

    /** 医保卡号 */
    private String medicalInsuranceNo;

    // 其他

    /** 预计入院时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectAdmitTime;

    /** 备注 */
    private String remark;
}
