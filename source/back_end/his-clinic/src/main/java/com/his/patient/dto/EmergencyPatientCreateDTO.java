package com.his.patient.dto;

import lombok.Data;

/**
 * 急诊建档创建患者主档的入参（三无患者允许没有身份证，姓名和性别不能省）。
 */
@Data
public class EmergencyPatientCreateDTO {

    /** 患者姓名 */
    private String patientName;

    private Integer gender;

    /** 年龄 */
    private Integer age;

    private String phone;

    /**
     * 身份证：急诊可以没有，但填了就要落库（否则 EMPI 同证去重拦不住急诊建档）
     */
    private String idCard;
}
