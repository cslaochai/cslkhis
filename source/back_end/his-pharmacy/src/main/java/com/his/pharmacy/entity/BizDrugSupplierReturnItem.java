package com.his.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 药品供应商退货明细（一行一个库存批次）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_supplier_return_item")
public class BizDrugSupplierReturnItem extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /** 退货单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long returnId;

    /** 库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品编码 */
    private String drugCode;

    /** 药品名称 */
    private String drugName;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 批号 */
    private String batchNo;

    /** 有效期（快照，退货理由多与效期有关） */
    private LocalDate expiryDate;

    /** 退货库位（快照，1-药库 2-药房） */
    private Integer stockRoom;

    /** 批次所属供应商ID（快照，建单时已校验与本单供应商一致） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /** 批次成本价（快照，退货金额口径） */
    private BigDecimal costPrice;

    /** 退货数量 */
    private BigDecimal quantity;

    /** 退货金额（数量×成本价） */
    private BigDecimal amount;
}
