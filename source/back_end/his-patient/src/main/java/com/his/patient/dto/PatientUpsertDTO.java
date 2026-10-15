package com.his.patient.dto;

import com.his.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 患者信息新增/修改入参
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
     * 性别（1-男 2-女 9-未知）
     */
    @NotNull(message = "性别不能为空（1-男 2-女 9-未知）")
    @AllowedValues(values = {"1", "2", "9"}, message = "性别取值不合法")
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
    /**
     * 联系人关系（父母、配偶、子女等）
     */
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
     * 婚姻状况：0-未婚 1-已婚 2-离异 3-丧偶
     */
    @AllowedValues(values = {"0", "1", "2", "3"}, message = "婚姻状况取值不合法")
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
     * 费别/参保类型（字典 medical_insurance_type：自费/城镇职工医保/城乡居民医保/公费医疗/商业医疗保险）；
     * 不传时默认自费
     */
    private String medicalInsuranceType;
    /**
     * 医保卡号（费别为医保类时填写）
     */
    private String medicalInsuranceNo;
    /**
     * 卡片类型（1-就诊卡 2-身份证 3-医保卡）
     */
    @AllowedValues(values = {"1", "2", "3"}, message = "卡片类型取值不合法")
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
    @AllowedValues(values = {"0", "1"}, message = "状态取值不合法")
    private Integer status;
}
