package com.his.drgdip.settle.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 按病种付费特例单议（病例申诉）实体（P4 结算层，DRG/DIP 共用）。
 * <p>官方口径：特例单议占结算病例 DRG ≤5%、DIP 2027 起 ≤1%。
 * 状态机：1-草稿 → 2-已提交 →（3-审核通过 / 4-审核驳回）；草稿可撤回（5-已撤回）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_case_appeal")
public class BizCaseAppeal extends BaseEntity {

    /**
     * 特例单议申请单号
     */
    private String appealNo;

    /**
     * 结算结果ID（biz_medpay_settle.id）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settleId;

    /**
     * 结算账单ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long settleBillId;

    /**
     * 病案首页ID（快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long summaryId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 付费方式（DRG / DIP，快照）
     */
    private String payMode;

    /**
     * 组码/病种码（快照）
     */
    private String groupCode;

    /**
     * 组名/病种名（快照）
     */
    private String groupName;

    /**
     * 申请类型（1-费用极高 2-重大新技术 3-危急重症 4-罕见病 5-其他）
     */
    private Integer reasonType;

    /**
     * 申请理由
     */
    private String reasonDesc;

    /**
     * 申请时盈亏（快照）
     */
    private BigDecimal profitAmount;

    /**
     * 状态（1-草稿 2-已提交 3-审核通过 4-审核驳回 5-已撤回）
     */
    private Integer status;

    /**
     * 申请人
     */
    private String applyBy;

    /**
     * 申请人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyById;

    /**
     * 提交时间
     */
    private LocalDateTime applyTime;

    /**
     * 审核人
     */
    private String reviewBy;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewById;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewRemark;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
