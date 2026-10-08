package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院证 / 入院通知单
 *
 * <p>这张表是「门诊 → 住院」唯一的凭据，也是三甲真实链路上原本缺失的那一环：
 * 门诊医生开证 → 入院处按证排床收治 → 入院记录回指来源挂号与来源证。
 *
 * <p>为什么单独建表而不复用转诊：转诊是「转诊到外院 / 转其他科室」
 * （有 {@code to_hospital} 字段），与"本院收治入院"是两种业务，混用会让后续统计口径无法区分。
 *
 * <p>患者信息全部是**开证时的快照**，不做 JOIN 实时取：住院证是"当时开出的那张纸"，
 * 患者后来改了手机号，证面不该跟着变——这是要有据可查的要求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_admission_order")
public class BizAdmissionOrder extends BaseEntity {

    /**
     * 住院证号（RZ + yyyyMMdd + 3位序号）
     */
    private String orderNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    // 来源门诊线索（"打通"的关键）

    /**
     * 来源挂号ID（挂号信息的ID，原挂号单）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 来源挂号号（快照，便于人读与对账）
     */
    private String registNo;

    /**
     * 来源就诊次ID（就诊次的就诊ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long visitId;

    // 开证方

    /**
     * 开证科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDeptId;

    /**
     * 开证科室名称
     */
    private String sourceDeptName;

    /**
     * 开证医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDoctorId;

    /**
     * 开证医生姓名
     */
    private String sourceDoctorName;

    // 拟收治（入院处可调整，调整必须留痕）

    /**
     * 拟收治科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDeptId;

    /**
     * 拟收治科室名称
     */
    private String applyDeptName;

    /**
     * 拟诊ICD编码
     */
    private String diagnosisCode;

    /**
     * 拟诊名称
     */
    private String diagnosisName;

    /**
     * 病情与收治说明
     */
    private String diagnosisNote;

    // 医保

    /**
     * 医保类型
     */
    private String insuranceType;

    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;

    // 状态机

    /**
     * 状态（1-待收治 2-已收治 3-已作废 4-已过期）
     */
    private Integer orderStatus;

    /**
     * 预计入院时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectAdmitTime;

    /**
     * 开证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 有效期至（超过即视为过期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    // 收治回填（闭环的回指）

    /**
     * 收治后回填的入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 实际收治时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime admitTime;

    /**
     * 实际收治科室ID（与拟收治不一致 = 入院处调过科）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admitDeptId;

    /**
     * 作废原因
     */
    private String cancelReason;
}
