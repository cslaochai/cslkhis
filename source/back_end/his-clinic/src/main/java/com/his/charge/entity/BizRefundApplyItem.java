package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;


/**
 * 退费申请明细行：一行 = 申请单要退的一条记账行 + 退费数量。
 *
 * <p>没有这张表之前，申请单只有「类型 + 金额」，圈选结果（退哪张处方的哪几味药、各退几盒）
 * 在执行那一刻靠金额贪心反推，且半条明细根本退不了。明细行把圈选结果落成凭证：
 * 审核人看到的就是收费处将要执行的动作。
 *
 * <p>金额与项目名是快照：执行前记账行可能又被别的退费冲过（remaining 变小），
 * 审批单必须定格在发起那一刻，不能跟着漂。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_refund_apply_item")
public class BizRefundApplyItem extends BaseEntity {

    /**
     * 退费申请ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 来源记账行ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long feeRecordId;

    /**
     * 本次退费数量（=该行剩余量时即整行退）
     */
    private BigDecimal refundQuantity;

    /**
     * 本次退费金额（快照）
     */
    private BigDecimal refundAmount;

    /**
     * 项目名称（快照）
     */
    private String itemName;

    /**
     * 来源单据号快照（处方号/申请单号；手工补记账无来源单为空）
     */
    private String sourceNo;

    /**
     * 逻辑删除标志（0 未删除 1 已删除）
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
