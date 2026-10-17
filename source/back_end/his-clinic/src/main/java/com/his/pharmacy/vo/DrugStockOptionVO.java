package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 可挂靠库存批次行（本模块 biz_drug_stock 的只读子集）。
 */
@Data
public class DrugStockOptionVO implements Serializable {

    /**
     * 库存批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 有效期至（已过期的批次不该被选中，采集时服务层会再挡一次）
     */
    private LocalDate expiryDate;

    /**
     * 库存数量，单位：最小包装单位
     */
    private BigDecimal quantity;

    /**
     * 可用数量，单位：最小包装单位
     */
    private BigDecimal availableQuantity;

    /**
     * 库存地点（1-药库 2-药房）
     */
    private Integer stockRoom;

    /**
     * 库位（货位）
     */
    private String location;

    /**
     * 供应商（文本快照）
     */
    private String supplier;

    /**
     * 供应商ID（可空：历史批次没有结构化供应商）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long supplierId;
}
