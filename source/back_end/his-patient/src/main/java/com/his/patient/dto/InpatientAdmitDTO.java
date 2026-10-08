package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.validation.InEnum;
import com.his.patient.enums.AdmitWayEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入院登记入参
 */
@Data
public class InpatientAdmitDTO {

    /**
     * 患者ID（必填）
     */
    private Long patientId;

    /**
     * 住院证ID（门诊转住院时必传）
     * <p>传了就按证的语义走：患者必须与证一致、入院途径强制为「门诊」、
     * 挂号/就诊线索从证上带过来，收治成功后回填 admission_id。
     * 不传则是"直接入院"（急诊/转院），入院途径必须由调用方显式给出。
     */
    private Long admissionOrderId;

    /**
     * 来源挂号ID（无住院证时用来挂门诊线索；急诊入院也走挂号，所以有这个字段）
     */
    private Long registId;

    /**
     * 来源挂号号
     */
    private String registNo;

    /**
     * 病区ID（必填）
     */
    private Long wardId;

    /**
     * 床位ID（必填，必须空闲）
     */
    private Long bedId;

    /**
     * 入院科室ID（不传则按病区推导）
     */
    private Long deptId;

    /**
     * 入院医生ID（必填）
     */
    private Long admitDoctorId;

    /**
     * 入院时间（不传取当前时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 入院途径：1-门诊 2-急诊 3-转院 4-其他（必填，病案首页字段）
     */
    @InEnum(value = AdmitWayEnum.class, message = "入院途径取值不合法（应为 1-门诊 2-急诊 3-转院 4-其他）")
    private Integer admitWay;

    /**
     * 入院诊断（文本）
     */
    private String diagnosis;

    /**
     * 入院诊断ICD编码
     */
    private String admitDiagnosisCode;

    /**
     * 入院诊断名称
     */
    private String admitDiagnosisName;

    /**
     * 备注
     */
    private String remark;
}
