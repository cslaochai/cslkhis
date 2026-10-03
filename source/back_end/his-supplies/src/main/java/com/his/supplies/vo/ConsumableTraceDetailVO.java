package com.his.supplies.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 高值耗材溯源详情VO：字典→入库批次→使用患者→计费全链（正向按码查档案）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumableTraceDetailVO extends BizConsumableTraceVO {
    // 字典段
    private String manufacturer;
    /** 类别 */
    private Integer category;

    // 入库批次段
    /** 批次当前剩余数量 */
    private BigDecimal batchQuantity;
    /** 批次存放位置 */
    private String location;
    /** 批次成本价 */
    private BigDecimal costPrice;
    /** 建批入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stockInTime;
    /** 建批入库经办（耗材批次库存的创建人） */
    private String stockInBy;

    // 计费段（记账行定位，供跳账单）
    /** 记账行ID */
    private Long feeRecordId;
    private BigDecimal chargeAmount;
}
