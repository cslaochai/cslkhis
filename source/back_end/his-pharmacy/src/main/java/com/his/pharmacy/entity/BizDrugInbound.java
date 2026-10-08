package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品入库单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_inbound")
public class BizDrugInbound extends BaseEntity {

    /** 入库单号（IN+yyyyMMdd+3位序号，唯一） */
    private String inboundNo;

    /** 入库类型（1-采购入库 2-退货入库 3-盘盈入库 4-其他入库） */
    private Integer inboundType;

    /** 来源采购订单ID（非采购来源为 NULL） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long purchaseOrderId;

    /** 来源采购订单号（写入时快照） */
    private String purchaseOrderNo;

    /** 供应商名称（文本快照，不联表） */
    private String supplier;

    /** 总金额 = Σ明细金额 */
    private BigDecimal totalAmount;

    /** 总数量 = Σ明细数量 */
    private BigDecimal totalQuantity;

    /** 入库状态（1-待审核 2-已审核 3-已入库 4-已取消） */
    private Integer inboundStatus;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 入库人 */
    private String inboundBy;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inboundTime;

    /** 取消人 */
    private String cancelBy;

    /** 取消时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /** 取消原因 */
    private String cancelReason;
}
