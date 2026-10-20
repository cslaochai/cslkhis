package com.his.spi.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 临床闭环（手术、输血等）回写正式文书的入参。
 * 正文由发起方组装，提供方只负责病历号取号与落库。
 */
@Data
public class ClosedLoopRecordDTO {

    private Long admissionId;

    /** 患者ID */
    private Long patientId;

    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    private Integer gender;

    /** 年龄 */
    private Integer age;

    private Integer ageUnit;

    private Long deptId;

    private String deptName;

    /** 病区ID */
    private Long wardId;

    /** 病区名称 */
    private String wardName;

    private String bedNo;

    /**
     * 文书类型（com.his.common.enums.InpatientRecordTypeEnum）
     */
    private Integer recordType;

    private String recordTitle;

    private LocalDateTime recordTime;

    private String diagnosisName;

    private String courseNote;

    private String treatmentPlan;

    /** 备注 */
    private String remark;

    /**
     * 文书状态（com.his.common.enums.InpatientRecordStatusEnum）
     */
    private Integer recordStatus;

    private Long doctorId;

    private String doctorName;

    /** 提交时间 */
    private LocalDateTime submitTime;
}
