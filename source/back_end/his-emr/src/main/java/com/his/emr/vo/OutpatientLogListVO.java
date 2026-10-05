package com.his.emr.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 门诊日志（法规台账）行。
 *
 * <p>一行 = 一次已提交/已归档的门诊病历（有诊断事实的接诊）。
 * 「可报/已报」不是新造的状态列，而是现算：诊断 ICD 命中法定传染病字典=可报，
 * 该挂号存在报告卡=已报；事实的唯一样本仍是传染病报告卡。
 *
 * <p>电话只在后端脱敏（{@code SensitiveMaskUtils}），出参只有 {@code phoneMasked}。
 */
@Data
public class OutpatientLogListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 就诊日期 yyyy-MM-dd（SQL DATE_FORMAT 出文本，避免时区歧义）
     */
    private String visitDate;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 明文出参一律置 null，仅保留脱敏列（改了出参就别让明文有机会漏出去）
     */
    private String phone;

    private String phoneMasked;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 体温原文（库里是 varchar，非数值不硬转）
     */
    private String temperature;

    /**
     * 发热：体温可解析且 ≥ 37.3℃
     */
    private Boolean fever;

    /**
     * 诊断 ICD 命中法定传染病目录
     */
    private Boolean reportable;

    /**
     * 命中的法定病种名（字典前缀反查，best effort）
     */
    private String matchedDiseaseName;

    /**
     * 该挂号已有报告卡
     */
    private Boolean reported;

    private String reportNo;

    /**
     * 报告卡状态（已有口径，不在本层重定义）
     */
    private Integer reportStatus;

    /**
     * 报卡诊断（与病历诊断不一致时，核查页要看得见）
     */
    private String reportDiseaseName;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）
     */
    private Integer signStatus;
}
