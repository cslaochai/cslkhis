package com.his.emr.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 门诊病历
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_medical_record")
public class BizMedicalRecord extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 病历号
     */
    private String recordNo;

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
     * 挂号ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 挂号单号
     */
    private String registNo;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 就诊类型（1-初诊 2-复诊）
     */
    private Integer visitType;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 主诉
     */
    private String chiefComplaint;

    /**
     * 现病史
     */
    private String presentIllness;

    /**
     * 既往史
     */
    private String pastHistory;

    /**
     * 个人史
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史
     */
    private String allergyHistory;

    /**
     * 体温（℃）
     */
    private String temperature;

    /**
     * 脉搏（次/分）
     */
    private String pulse;

    /**
     * 呼吸（次/分）
     */
    private String respiration;

    /**
     * 收缩压（mmHg）
     */
    private String systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private String diastolicPressure;

    /**
     * 一般情况
     */
    private String generalCondition;

    /**
     * 皮肤黏膜
     */
    private String skinMucosa;

    /**
     * 头颈部
     */
    private String headNeck;

    /**
     * 胸肺
     */
    private String chestLung;

    /**
     * 心脏
     */
    private String heart;

    /**
     * 腹部
     */
    private String abdomen;

    /**
     * 脊柱四肢
     */
    private String spineLimbs;

    /**
     * 神经系统
     */
    private String nervousSystem;

    /**
     * 专科检查
     */
    private String specialistExam;

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断（主诊断+次诊断）
     */
    private String diagnosis;

    /**
     * 诊断编码
     */
    private String diagnosisCode;

    /**
     * 诊断名称
     */
    private String diagnosisName;

    /**
     * 处理意见
     */
    private String treatmentPlan;

    /**
     * 病历状态（1-草稿 2-已提交 3-已归档 4-已作废）
     */
    private Integer recordStatus;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 审核状态（0-待提交 1-待审核 2-审核通过 3-审核驳回）
     */
    private Integer reviewStatus;

    /**
     * 审核人
     */
    private String reviewBy;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewRemark;

    /**
     * 患者引导单PDF路径
     */
    private String guidePdfPath;

    /**
     * 处方数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer prescriptionCount;
    /**
     * 检查检验数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer inspectionCount;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）
     *
     * <p>P5.5 起：`1` = 已由接诊医生签名，**签名即锁定**（再次保存会被拒绝）；
     * `2` = 签名被作废过（不是"从来没签过"，不允许回落成 0）。
     */
    private Integer signStatus;

    /**
     * 当前有效签名ID（电子签名证据的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long signId;

    /**
     * 最近一次签名时刻
     */
    private LocalDateTime signedTime;
}
