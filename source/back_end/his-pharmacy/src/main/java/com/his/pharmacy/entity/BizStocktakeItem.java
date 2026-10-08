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
 * 药房盘点明细（一行一批次）
 * <p>药品名/规格/批号/成本价在建单时从字典与批次快照写入，不靠联表回显：
 * 盘点单是账实差异的凭证，事后改名不能改变当时的记载。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stocktake_item")
public class BizStocktakeItem extends BaseEntity {
    /**
     * 盘点单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stocktakeId;

    /**
     * 库存批次ID（药品批次库存主键）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /**
     * 药品ID
     */
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

    /** 生产日期 */
    private LocalDate productionDate;

    /** 有效期 */
    private LocalDate expiryDate;

    /** 库位 */
    private String location;

    /** 成本价（快照，差异金额口径） */
    private BigDecimal costPrice;

    /** 快照时已锁定数量（已开方未发药） */
    private BigDecimal lockedQuantity;

    /** 账面数量（快照时点） */
    private BigDecimal bookQuantity;

    /** 实盘数量（NULL=尚未录入） */
    private BigDecimal countedQuantity;

    /** 差异数量（实盘-账面，未录=0；盈正亏负） */
    private BigDecimal diffQuantity;

    /** 差异金额（差异数量×成本价，元） */
    private BigDecimal diffAmount;

    /** 过账标记（0-未过账 1-已盘盈亏过账 2-无差异免过账） */
    private Integer posted;
}
