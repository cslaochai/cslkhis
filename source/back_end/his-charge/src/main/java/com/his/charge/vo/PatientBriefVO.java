package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.charge.api.PatientGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者跨域摘要：收费域只读患者主索引的最小字段集。
 */
@Data
@NoArgsConstructor
public class PatientBriefVO {

    /**
     * 患者ID（biz_patient 主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄（岁）
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号（医保结算身份核对用）
     */
    private String idCard;

    /**
     * 患者类型
     */
    private Integer patientType;

    /**
     * 医保类型
     */
    private String medicalInsuranceType;

    /**
     * 医保卡号/医保编号（空串 = 无医保，按自费结算）
     */
    private String medicalInsuranceNo;
}
