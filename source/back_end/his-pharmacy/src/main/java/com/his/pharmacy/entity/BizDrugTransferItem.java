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
 * 药品调拨明细（一行一个发出方批次）
 *
 * <p>药品名/批号/效期/成本在建单时全部快照，接收方落位也按这份快照找同批号批次：
 * 调拨单是「货在两个库位之间搬」的凭证，事后改字典不能改变当时的记载。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_transfer_item")
public class BizDrugTransferItem extends BaseEntity {
    /** 调拨单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long transferId;

    /** 发出方库存批次ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /** 接收方库存批次ID（接收确认时回写，未接收为 NULL） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long inStockId;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品编码（快照） */
    private String drugCode;

    /** 药品名称（快照） */
    private String drugName;

    /** 规格（快照） */
    private String specification;

    /** 单位（快照） */
    private String unit;

    /** 批号（快照，接收方按同批号同效期落位） */
    private String batchNo;

    /** 生产日期（快照） */
    private LocalDate productionDate;

    /** 有效期（快照） */
    private LocalDate expiryDate;

    /** 批次成本价（快照，金额口径） */
    private BigDecimal costPrice;

    /** 调拨数量（建单即定，发出与接收都按它走，不做部分数量） */
    private BigDecimal applyQuantity;

    /** 建单时该批次已锁定量（已开方未发药，快照只用于报错说明） */
    private BigDecimal lockedQuantity;

    /** 发出标记（0-未发出 1-已发出） */
    private Integer outFlag;

    /** 接收标记（0-未接收 1-已接收） */
    private Integer inFlag;
}
