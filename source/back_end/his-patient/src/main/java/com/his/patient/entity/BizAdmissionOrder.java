package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 住院证 / 入院通知单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_admission_order")
public class BizAdmissionOrder extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
