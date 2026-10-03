package com.his.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者信息新增/修改入参
 *
 * <p>姓名 / 性别 / 身份证号三项必填 —— 它们既是 EMPI 去重的匹配键，
 * 也是下游性别判断与实名核验的依据；其余字段（民族、职业、婚姻、血型、
 * 联系人、医保等）允许先建档后补录。
 *
 * <p>手机号<b>不</b>在此强制：老年患者没有手机号是常态，硬拦只会逼出乱填的号码，
 * 而手机号是 EMPI 匹配键之一 —— 编出来的号比空号危害大。填了则必须合法
 * （由 {@code PatientProfileValidator} 校验）。
 */
@Data
public class PatientUpsertDTO {
    /**
     * 患者ID，新增时为空，修改时必填
     */
    private Long id;
    /**
     * 患者号（病历号/就诊卡号）
     */
    private String patientNo;
    /**
     * 患者姓名
     */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;
    /**
     * 性别：1-男 2-女 3-未知（必选，不允许留空靠默认值兜底）
     */
    @NotNull(message = "性别不能为空（1-男 2-女 3-未知）")
    private Integer gender;
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    /**
     * 年龄（岁），新增时由出生日期自动计算
     */
    private Integer age;
    /**
     * 身份证号（18 位；新增时校验位必须正确，修改时只验格式）
     */
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 紧急联系人姓名
     */
    private String contactName;
    /**
     * 紧急联系人电话
     */
    private String contactPhone;
    /** 联系人关系（父母、配偶、子女等） */
    private String contactRelation;
    /**
     * 家庭住址
     */
    private String address;
    /**
     * 民族
     */
    private String nation;
    /**
     * 职业
     */
    private String occupation;
    /**
     * 婚姻状况：1-未婚 2-已婚 3-离异 4-丧偶
     */
    private Integer maritalStatus;
    /**
     * 血型（如：A、B、O、AB）
     */
    private String bloodType;
    /**
     * 过敏史描述
     */
    private String allergyHistory;
    /**
     * 既往病史描述
     */
    private String medicalHistory;
    /**
     * 患者类型（参保性质）：1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他
     * <p>口径与患者基本信息的患者类型列注释、字典数据(his_patient_type) 一致。
     * 注意别与「就诊类型（门诊/住院/急诊）」混淆——那是另一套码值。
     */
    private Integer patientType;
    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;
    /**
     * 医保类型（如：职工医保、居民医保）
     */
    private String medicalInsuranceType;
    /** 卡片类型（1-就诊卡 2-身份证 3-医保卡） */
    private Integer cardType;
    /**
     * 证件号码
     */
    private String cardNo;
    /**
     * 账户余额，单位：元
     */
    private BigDecimal balance;
    /**
     * 累计消费金额，单位：元
     */
    private BigDecimal totalExpense;
    /**
     * 就诊次数
     */
    private Integer visitCount;
    /**
     * 最近就诊科室ID
     */
    private Long lastVisitDept;
    /**
     * 最近就诊医生ID
     */
    private Long lastVisitDoctor;
    /**
     * 患者照片URL
     */
    private String photo;
    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
}
