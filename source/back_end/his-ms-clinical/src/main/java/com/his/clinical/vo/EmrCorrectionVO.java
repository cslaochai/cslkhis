package com.his.clinical.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 病历更正申请 VO
 */
@Data
public class EmrCorrectionVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 更正申请单号 CR+yyyyMMdd+4位
     */
    private String requestNo;

    /**
     * 病历ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 就诊挂号单ID（取自病历，跳转用）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long registId;

    /**
     * 病历号（快照）
     */
    private String recordNo;

    /**
     * 患者ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 就诊日期（取自病历）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    /**
     * 病历所属科室ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 病历所属科室名称（快照）
     */
    private String deptName;

    /**
     * 原经治医生ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long originalDoctorId;

    /**
     * 原经治医生姓名（快照）
     */
    private String originalDoctorName;

    /**
     * 申请人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicantId;

    /**
     * 申请人姓名（快照）
     */
    private String applicantName;

    /**
     * 更正类型（1-经治医生更正 2-上级代改）
     */
    private Integer correctionType;

    /**
     * 更正类型文案
     */
    private String correctionTypeText;

    /**
     * 更正原因/事由
     */
    private String reason;

    /**
     * 拟更正内容说明
     */
    private String plannedContent;

    /**
     * 状态（1-待科室审核 2-待医务审批 3-已批准待修改 4-已驳回 5-已完成 6-已撤回）
     */
    private Integer status;

    /**
     * 状态文案
     */
    private String statusText;

    /**
     * 科室审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptApproverId;

    /**
     * 科室审核人姓名（快照）
     */
    private String deptApproverName;

    /**
     * 科室审核时间
     */
    private LocalDateTime deptApproveTime;

    /**
     * 科室审核意见
     */
    private String deptComment;

    /**
     * 医务审批人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long medicalApproverId;

    /**
     * 医务审批人姓名（快照）
     */
    private String medicalApproverName;

    /**
     * 医务审批时间
     */
    private LocalDateTime medicalApproveTime;

    /**
     * 医务审批意见
     */
    private String medicalComment;

    /**
     * 实际修改人员工ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long modifierId;

    /**
     * 实际修改人姓名（快照）
     */
    private String modifierName;

    /**
     * 病历回写完成时间
     */
    private LocalDateTime modifyTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
