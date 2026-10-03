package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检查申请出参
 */
@Data
public class BizInspectionApplyVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请单号
     */
    private String applyNo;

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
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 病历号
     */
    private String recordNo;

    /**
     * 就诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 申请科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 申请科室名称
     */
    private String deptName;

    /**
     * 申请医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long doctorId;

    /**
     * 申请医生姓名
     */
    private String doctorName;

    /**
     * 检查项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionItemId;

    /**
     * 检查项目编码
     */
    private String inspectionItemCode;

    /**
     * 检查项目名称
     */
    private String inspectionItemName;

    /**
     * 检查科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inspectionDeptId;

    /**
     * 检查科室名称
     */
    private String inspectionDeptName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 检查目的
     */
    private String inspectionPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 病史摘要
     */
    private String diseaseSummary;

    /**
     * 特殊要求
     */
    private String specialRequirements;

    /**
     * 是否急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 检查费用，单位：元
     */
    private BigDecimal price;

    /**
     * 申请单状态（批次E/E4 收口为三态：1-已提交 2-已缴费 6-已取消）。
     * <p>执行进度<b>不再</b>写在这里 —— 那会让申请单状态与检查记录状态两套码值撞车
     * （旧值 3/4/5 在新码表里含义完全不同）。要看进度请读 {@link #execStatusText}。
     */
    private Integer applyStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 预约时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appointmentTime;

    // 执行进度（批次E/E5：由 his-medicaltech 提供，见 ApplyExecStatusGateway）

    /**
     * 对应的检查记录ID（未缴费 / 未建记录时为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execRecordId;

    /**
     * 检查记录状态码（1已登记 2已签到 3检查中 4已出结果 5已审核 6已发布 7已取消）
     */
    private Integer execStatus;

    /**
     * 面向医生的进度文案（已缴费待执行 / 已到检 / 检查中 / 已出结果 / 已审核 / 已取消）。
     * <b>由后端给</b>：检查与检验的 record_status 是两套不同码表，前端按码值翻译必然翻错。
     */
    private String execStatusText;

    /**
     * 是否存在未作废的危急值（医生站要高亮）
     */
    private Boolean critical;

    /**
     * 是否允许删除（后端判定，前端不自判）
     */
    private Boolean canDelete;

    /**
     * 不允许删除的原因（canDelete=false 时给出可读文案）
     */
    private String deleteBlockReason;

    /**
     * 签名状态（0-未签名 1-已签名 2-签名已失效）—— 开单即签，签名即锁定
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
