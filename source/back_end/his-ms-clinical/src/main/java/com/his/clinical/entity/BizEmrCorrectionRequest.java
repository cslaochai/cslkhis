package com.his.clinical.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 病历更正申请单。
 *
 * <p>已提交/归档病历原则上不可直接修改；确需更正的，由经治医生或上级发起申请，
 * 科室初审、医务部门终审批准后授权回写，修改人与修改时间、前后值全程留痕。
 * 病历归属快照不变，本表只承载审批流与更正留痕。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emr_correction_request")
public class BizEmrCorrectionRequest extends BaseEntity {

    /**
     * 逻辑删除标志（0-正常 1-删除）
     */
    @TableLogic
    private Integer delFlag;

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
     * 非数据库字段：状态文案（出参翻译）
     */
    @TableField(exist = false)
    private String statusText;

    /**
     * 非数据库字段：更正类型文案（出参翻译）
     */
    @TableField(exist = false)
    private String correctionTypeText;
}
