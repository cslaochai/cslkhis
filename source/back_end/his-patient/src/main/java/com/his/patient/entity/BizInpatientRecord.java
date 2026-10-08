package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 住院病历文书（P2：结构化要素的载体）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_record")
public class BizInpatientRecord extends BaseEntity implements Serializable {

    /**
     * 病历文书号（BL + yyyyMMdd + 4位序号）
     */
    private String recordNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 性别：1-男 2-女 9-未知
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 年龄单位：1-岁 2-月 3-天
     */
    private Integer ageUnit;

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
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 文书类型：1-入院记录 2-首次病程 3-日常病程 4-术前小结 5-手术记录 6-术后首次病程 7-出院记录 8-死亡记录
     */
    private Integer recordType;

    /**
     * 文书标题（默认取类型文案，可自定）
     */
    private String recordTitle;

    /**
     * 记录时间（病程记录 = 记录时刻；入院记录 = 入院时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    // 结构化要素：病史

    /**
     * 主诉（症状 + 持续时间）
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
     * 个人史（含婚育、烟酒、职业）
     */
    private String personalHistory;

    /**
     * 家族史
     */
    private String familyHistory;

    /**
     * 过敏史（无过敏史也必须显式写"否认"，不允许留空代替否认）
     */
    private String allergyHistory;

    // 结构化要素：生命体征（数值列）

    /**
     * 体温（℃）
     */
    private BigDecimal temperature;

    /**
     * 脉搏（次/分）
     */
    private Integer pulse;

    /**
     * 呼吸（次/分）
     */
    private Integer respiration;

    /**
     * 收缩压（mmHg）
     */
    private Integer systolicPressure;

    /**
     * 舒张压（mmHg）
     */
    private Integer diastolicPressure;

    /**
     * 身高（cm）
     */
    private BigDecimal height;

    /**
     * 体重（kg）
     */
    private BigDecimal weight;

    // 结构化要素：体格检查（按系统拆列）

    /**
     * 一般情况（神志/发育/营养/体位/面容）
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
     * 胸部及肺
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

    // 结构化要素：诊疗过程与结论

    /**
     * 辅助检查
     */
    private String auxiliaryExam;

    /**
     * 诊断名称（多诊断用分号分隔）
     */
    private String diagnosisName;

    /**
     * 诊断编码（ICD-10）
     */
    private String diagnosisCode;

    /**
     * 诊疗计划 / 处理意见
     */
    private String treatmentPlan;

    /**
     * 病程记录正文（仅病程类文书使用）
     */
    private String courseNote;

    // 状态与留痕

    /**
     * 文书状态：1-草稿 2-已提交 3-已归档（归档后禁改）
     */
    private Integer recordStatus;

    /**
     * 书写医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 书写医生姓名
     */
    private String doctorName;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 归档时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archiveTime;

    /**
     * 归档人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long archiveBy;

    /**
     * 归档人姓名
     */
    private String archiveByName;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）
     *
     * <p>P5.5 起：`1` 表示这份文书已被书写医生签名，**签名即锁定**（update 会被拒绝）；
     * `2` 表示签名被作废过（不等于"从来没签过"，不允许回落成 0）。
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signedTime;
}
