package com.his.patient.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 患者主档（读侧契约）。
 */
@Data
public class PatientMasterVO {

    /** 主键ID */
    private Long id;

    /** 患者编号 */
    private String patientNo;

    /** 患者姓名 */
    private String patientName;

    /** 性别（1-男 2-女 9-未知） */
    private Integer gender;

    /** 出生日期 */
    private LocalDate birthDate;

    /** 年龄 */
    private Integer age;

    /** 身份证号 */
    private String idCard;

    /** 联系电话 */
    private String phone;

    /** 家庭住址 */
    private String address;

    /** 血型（如：A、B、O、AB） */
    private String bloodType;

    /** 主索引：已并入的主档患者ID */
    private Long masterId;

    /** 主索引状态（0-正常 1-已并入主档） */
    private Integer mergeStatus;

    /**
     * 过敏史文本（与结构化过敏表并存）
     */
    private String allergyHistory;

    /** 医保类型（自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险） */
    private String medicalInsuranceType;

    /** 医保卡号 */
    private String medicalInsuranceNo;

    /** 删除标志（0-正常 1-已删除） */
    private Integer delFlag;
}
