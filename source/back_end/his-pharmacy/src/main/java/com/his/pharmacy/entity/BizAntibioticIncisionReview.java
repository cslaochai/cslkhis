package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * I 类切口手术围手术期预防用药点评（专项整治必查）。
 *
 * <p>一台手术一条（uk_incision_apply）。预防用药信息由后端从住院医嘱自动带出候选，
 * 点评人确认/纠正后提交结论 —— 系统是"把证据摆上台面"，结论由药师下。
 *
 * <p>点评维度：有无指征(41) / 品种选择(42) / 给药时机(43) / 疗程(44) / 联合用药(45) / 剂量(46) /
 * 特殊使用级会诊(47) / 时机无法判定(48)。问题码 4x 段与结论 2-不合理必须同现。
 *
 * <p>无 del_flag，删除走物理删。
 */
@Data
@TableName("biz_antibiotic_incision_review")
public class BizAntibioticIncisionReview implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 点评结论：合理 */
    public static final int RESULT_REASONABLE = 1;
    /** 点评结论：不合理 */
    public static final int RESULT_UNREASONABLE = 2;

    /** 预防用药总时长上限（小时）：超过即"疗程过长" */
    public static final int MAX_PROPHYLACTIC_HOURS = 24;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 点评编号（KQI+yyyyMMdd+4位序号） */
    private String reviewNo;

    /** 手术申请单ID（手术申请单的ID） */
    private Long operationApplyId;

    /** 手术申请单号 */
    private String applyNo;

    /** 入院ID */
    private Long admissionId;

    /** 患者ID */
    private Long patientId;

    /** 患者姓名 */
    private String patientName;

    /** 手术科室 */
    private String deptName;

    /** 手术名称 */
    private String operationName;

    /** 手术编码 ICD-9-CM-3 */
    private String operationCode;

    /** 手术开始时间（快照，判定给药时机的锚点） */
    private LocalDateTime operationTime;

    /** 主刀医师 */
    private String surgeonName;

    /** 切口等级（固定 1-Ⅰ类） */
    private Integer incisionLevel;

    /** 预防用药药品ID */
    private Long drugId;

    /** 预防用药名称 */
    private String drugName;

    /** 预防用药分级（快照：1/2/3） */
    private Integer antibioticLevel;

    /** 是否有预防用药指征（0-无 1-有） */
    private Integer indicationFlag;

    /** 给药时机（1术前0.5~1h 2术前>1h 3术前&lt;0.5h 4术中追加 5术后才开始 6未使用） */
    private Integer timingType;

    /** 预防用药总时长（小时） */
    private Integer courseHours;

    /** 是否联合用药（0-否 1-是） */
    private Integer comboFlag;

    /** 联合用药理由 */
    private String comboReason;

    /** 特殊使用级是否有抗菌药物管理工作组会诊同意（0-无 1-有） */
    private Integer consultFlag;

    /** 点评结论（1-合理 2-不合理） */
    private Integer reviewResult;

    /** 问题码（逗号分隔 41~48） */
    private String problemTypes;

    /** 点评意见 */
    private String reviewOpinion;

    /** 点评人员工ID */
    private Long reviewerId;

    /** 点评人姓名 */
    private String reviewerName;

    /** 点评时间 */
    private LocalDateTime reviewTime;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;
}
