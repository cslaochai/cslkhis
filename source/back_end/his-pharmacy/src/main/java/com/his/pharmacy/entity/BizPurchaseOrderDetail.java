package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单明细
 *
 * 一张采购单买哪些药、什么批号与效期、多少数量与进价 —— 入库时据此建/加药品批次。
 * 主键列是 `id`，可继承 BaseEntity。
 *
 * ⚠ 「订单+药品+批号」的唯一键**不含删除标记**：
 *   逻辑删除后重录"同订单+同药品+同批号"会撞唯一键。
 *   所以明细**不做逻辑删除**，订单编辑/删除时一律**物理删除**明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_purchase_order_detail")
public class BizPurchaseOrderDetail extends BaseEntity {

    /** 采购订单ID（采购订单主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    /** 药品ID（药品字典主键） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 采购数量（最小包装单位） */
    private BigDecimal quantity;

    /** 采购单价（进价；入库时作为批次成本价） */
    private BigDecimal unitPrice;

    /** 金额 = 数量 × 单价（服务端重算） */
    private BigDecimal amount;

    /** 批号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期（必填，缺了无法建批次） */
    private LocalDate expiryDate;
}
