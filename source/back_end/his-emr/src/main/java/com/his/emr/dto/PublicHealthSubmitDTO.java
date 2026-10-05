package com.his.emr.dto;

import lombok.Data;

/**
 * 提交公卫上报入参
 */
@Data
public class PublicHealthSubmitDTO {

    /**
     * 上报单号
     */
    private String reportNo;

    /**
     * 患者ID
     */
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病历ID
     */
    private Long recordId;

    /**
     * 上报类型（1-传染病 2-死因监测 3-慢性病 4-其他）
     */
    private Integer reportType;

    /**
     * 上报内容
     */
    private String reportContent;

    /**
     * 诊断
     */
    private String diagnosis;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 上报人
     */
    private String reportBy;

}
