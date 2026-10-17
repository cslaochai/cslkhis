package com.his.provider.vo;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 病历简要信息
 */
@Data
@NoArgsConstructor
public class RecordBriefVO {
    /**
     * 病历ID
     */
    private Long id;
    /**
     * 病历号
     */
    private String recordNo;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 就诊日期
     */
    private LocalDate visitDate;
    /**
     * 就诊类型（1-初诊 2-复诊），见 VisitTypeEnum
     */
    private Integer visitType;
    /**
     * 原就诊医生ID
     */
    private Long doctorId;
    /**
     * 医生姓名
     */
    private String doctorName;
    /**
     * 原就诊科室ID
     */
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 诊断名称
     */
    private String diagnosisName;
}