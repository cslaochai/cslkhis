package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 可挂靠库存批次行（本模块 {@code biz_drug_stock} 的只读子集）。
 *
 * <p>对应 {@code BizDrugTraceMapper#selectStockOptions}：采集追溯码时选批次用，
 * SQL 已按 {@code stock_room ASC, expiry_date ASC, id ASC} 排好 —— 即 FEFO
 * （先到期先出），且药库(1) 排在药房(2) 前面，所以前端拿到列表直接选第一个即可，
 * 不用也不该在浏览器里再排一次序。
 *
 * <p>与出参 {@code DrugTraceScanVO.BatchOption} 分开：那个是给小程序/前端看的
 * （带 {@code @JsonFormat}/{@code @JsonSerialize} 注解），这个是 Mapper 的行承载，
 * 两者字段有意保持同形，便于逐字段搬。
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
