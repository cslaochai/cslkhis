package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 用血分级审批流水（sql/93）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfusion_approve")
public class BizTransfusionApprove extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 输血申请单ID（输血申请单的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 输血申请单号（冗余快照，防主单号变更）
     */
    private String applyNo;

    /**
     * 审批级别（1-上级医师 2-科主任 3-医务科）
     */
    private Integer approveLevel;

    /**
     * 审批结论（1-通过 2-驳回）
     */
    private Integer approveResult;

    /**
     * 审批人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批人职称（快照，如主治医师/科主任）
     */
    private String approverTitle;

    /**
     * 审批意见（驳回必填原因）
     */
    private String opinion;

    /**
     * 是否急诊补审（0-常规 1-补审）
     */
    private Integer isMakeup;
}
