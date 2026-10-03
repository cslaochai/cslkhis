package com.his.emr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检验申请出参
 */
@Data
public class BizLaboratoryApplyVO {
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
     * 检验项目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryItemId;

    /**
     * 检验项目编码
     */
    private String laboratoryItemCode;

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 检验科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long laboratoryDeptId;

    /**
     * 检验科室名称
     */
    private String laboratoryDeptName;

    /**
     * 标本类型
     */
    private String specimenType;

    /**
     * 检验目的
     */
    private String laboratoryPurpose;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 病史摘要
     */
    private String diseaseSummary;

    /**
     * 是否空腹（0-否 1-是）
     */
    private Integer isFasting;

    /**
     * 是否急诊（0-否 1-是）
     */
    private Integer isEmergency;

    /**
     * 检验费用，单位：元
     */
    private BigDecimal price;

    /**
     * 申请单状态（批次E/E4 收口为三态：1-已提交 2-已缴费 6-已取消）。
     * <p>执行进度<b>不再</b>写在这里（旧值 3=已采样 4=检验中 5=已出报告与检验记录码表撞车）；
     * 要看进度请读 {@link #execStatusText}。
     */
    private Integer applyStatus;

    /**
     * 提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    // 执行进度（批次E/E5：由 his-medicaltech 提供，见 ApplyExecStatusGateway）

    /**
     * 对应的检验记录ID（未缴费 / 未建记录时为空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long execRecordId;

    /**
     * 检验记录状态码（1已登记 2已采样 3已接收 4检测中 5已出结果 6已审核 7已发布 8已取消）
     */
    private Integer execStatus;

    /**
     * 面向医生的进度文案（已缴费待执行 / 已采样 / 检测中 / 已出结果 / 已审核 / 已取消）
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
