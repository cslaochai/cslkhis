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
