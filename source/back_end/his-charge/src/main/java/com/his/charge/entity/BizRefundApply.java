package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退费申请单
 */
@Data
@TableName("biz_refund_apply")
public class BizRefundApply {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 退费申请号
     */
    private String refundApplyNo;

    /**
     * 原结算账单ID（L2）。旧模型这里挂的是收费单ID，四层后收费单已退役：
     * 退费退的是"患者为这张账单掏过的钱"，锚点必须是账单，不是任何状态列。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long billId;
    /**
     * 结算账单号
     */
    private String billNo;

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
     * 退费类型（1-退药 2-退检查 3-退检验 4-退治疗 5-全部退费）
     */
    private Integer refundType;
    /**
     * 退费原因
     */
    private String refundReason;
    /**
     * 退费金额
     */
    private BigDecimal refundAmount;

    /**
     * 申请状态（字典 {@code his_refund_apply_status}：1-待审核 2-审核通过 3-审核驳回 4-已退费 5-已作废）
     */
    private Integer applyStatus;
    /**
     * 申请人
     */
    private String applyBy;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long auditorId;
    /**
     * 审核人姓名
     */
    private String auditorName;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
    /**
     * 审核意见
     */
    private String auditRemark;

    /**
     * 退费人
     */
    private String refundBy;
    /**
     * 退费时间
     */
    private LocalDateTime refundTime;

    /**
     * 作废人姓名（快照）。
     *
     * <p>作废只作用于 1-待审核 / 2-审核通过：审核通过后才发现"金额落不到明细边界"或
     * "患者又回来要做检查了"，没有作废入口就只能来库里改状态 —— 判重会把这张收费单永久锁死。
     */
    private String cancelBy;
    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    /**
     * 作废原因
     */
    private String cancelReason;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
