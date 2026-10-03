package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用血分级审批流水（sql/93）。
 *
 * <p><b>只增不改</b>：审批被谁通过/驳回、什么意见，是追溯的唯一抓手；
 * 主单上的 {@code approve_status} 只是当前结论的派生摘要，历史结论在这里。
 * 驳回后修改重提会产生新记录，旧记录不删不改。
 *
 * <p>id/createBy/createTime/updateBy/updateTime/delFlag/remark 由 {@link BaseEntity} 承载。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfusion_approve")
public class BizTransfusionApprove extends BaseEntity {

    /** 输血申请单ID（输血申请单的ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /** 输血申请单号（冗余快照，防主单号变更） */
    private String applyNo;

    /** 审批级别（1-上级医师 2-科主任 3-医务科） */
    private Integer approveLevel;

    /** 审批结论（1-通过 2-驳回） */
    private Integer approveResult;

    /** 审批人ID（员工ID） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long approverId;

    /** 审批人姓名（快照） */
    private String approverName;

    /** 审批人职称（快照，如主治医师/科主任） */
    private String approverTitle;

    /** 审批意见（驳回必填原因） */
    private String opinion;

    /** 是否急诊补审（0-常规 1-补审） */
    private Integer isMakeup;
}
