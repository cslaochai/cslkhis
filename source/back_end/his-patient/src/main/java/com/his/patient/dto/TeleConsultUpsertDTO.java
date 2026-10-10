package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 远程会诊申请 / 修改入参（仅「待安排」可改）。
 */
@Data
public class TeleConsultUpsertDTO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 患者ID
     */
    @NotNull(message = "患者不能为空")
    private Long patientId;

    /**
     * 关联住院ID
     */
    private Long admissionId;

    /**
     * 申请科室ID
     */
    private Long applyDeptId;

    /**
     * 申请医生ID（员工ID）
     */
    private Long applyDoctorId;

    /**
     * 会诊类型（1-临床会诊 2-远程影像 3-远程心电 4-远程病理 5-其他）
     */
    @NotNull(message = "会诊类型不能为空")
    private Integer consultType;

    /**
     * 是否急会诊（0-否 1-是）
     */
    private Integer isUrgent;

    /**
     * 受邀专家所在医院
     */
    private String expertHospital;

    /**
     * 受邀专家科室
     */
    private String expertDept;

    /**
     * 受邀专家姓名
     */
    private String expertName;

    /**
     * 受邀专家职称
     */
    private String expertTitle;

    /**
     * 会诊目的
     */
    @NotBlank(message = "会诊目的不能为空")
    private String purpose;

    /**
     * 申请方诊断/病情摘要
     */
    private String diagnosis;

    /**
     * 会诊费用
     */
    private BigDecimal fee;

    /**
     * 备注
     */
    private String remark;
}
