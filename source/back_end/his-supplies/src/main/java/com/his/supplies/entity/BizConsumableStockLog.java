package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 耗材出入库流水（耗材出入库流水）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_consumable_stock_log")
public class BizConsumableStockLog extends BaseEntity {
    /**
     * 库存批次ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stockId;

    /**
     * 耗材ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;

    /**
     * 批号
     */
    private String batchNo;

    /**
     * 变动类型（1-入库 2-领用出库 3-退回入库 4-其他出库 5-盘盈 6-盘亏）
     */
    private Integer changeType;

    /**
     * 变动数量（正=入负=出）
     */
    private BigDecimal changeQuantity;

    /**
     * 变动前批次数量
     */
    private BigDecimal quantityBefore;

    /**
     * 变动后批次数量
     */
    private BigDecimal quantityAfter;

    /**
     * 来源类型（consume-科室领用 manual-手工）
     */
    private String sourceType;

    /**
     * 来源单据ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    /**
     * 来源单据号
     */
    private String sourceNo;

    /**
     * 操作人
     */
    private String operatorName;
}
