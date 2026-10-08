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
 * 药品批次库存
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_drug_stock")
public class BizDrugStock extends BaseEntity {
    /**
     * 药品ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /**
     * 批号
     */
    private String batchNo;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 有效期
     */
    private LocalDate expiryDate;

    /**
     * 库存数量
     */
    private BigDecimal quantity;

    /**
     * 锁定数量（已开方未发药）
     */
    private BigDecimal lockedQuantity;

    /**
     * 可用数量
     */
    private BigDecimal availableQuantity;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 库存金额
     */
    private BigDecimal totalAmount;

    /**
     * 存放位置（货位，如「药库整件区-03」，与 stockRoom 是两个层级）
     */
    private String location;

    /**
     * 库存地点（1-药库 2-药房，字典 his_stock_room，见 StockRoomEnum，sql/154）
     * <p>只有先分成两层，「药房退回药库」这句话才有落点。
     */
    private Integer stockRoom;

    /**
     * 供应商名称（快照，展示用；退货看 supplierId）
     */
    private String supplier;

    /**
     * 供应商ID（供应商主档主键，sql/154）
     * <p>退货给谁必须结构化可判：原先只有 supplier 文本（里面装的多是生产厂家名），
     * 判"这批货是谁供的"要 log→入库明细→入库单三级文本拼接。没有本列的批次不允许退给供应商。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;

    /**
     * 库存状态（1-正常 2-预警 3-缺货 4-过期）
     */
    private Integer stockStatus;
}
