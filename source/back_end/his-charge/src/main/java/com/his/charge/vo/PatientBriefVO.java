package com.his.charge.vo;

import com.his.charge.api.PatientGateway;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 患者跨域摘要：收费域只读患者主索引的最小字段集。
 *
 * <p><b>为什么不让 charge 直接碰 {@code BizPatient} 实体：</b>跨模块只能依赖对方的 service，
 * 不能依赖对方的 Mapper 和实体（实体等于把对方的表结构泄漏成自己的编译期契约，
 * 对方加字段/改名就会把这里编译打断）。所以 charge 声明自己需要的形状，
 * 由 his-patient 负责把 {@code BizPatient} 映射过来。
 *
 * <p>字段只增不改：charge 侧任何新增诉求都走新增字段，不要把对方的宽实体整个搬进来。
 *
 * @see PatientGateway
 */
@Data
@NoArgsConstructor
public class PatientBriefVO {

    /**
     * 患者ID（biz_patient 主键）
     */
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
