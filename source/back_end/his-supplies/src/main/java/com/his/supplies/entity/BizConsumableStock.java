package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 耗材批次库存 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_consumable_stock")
public class BizConsumableStock extends BaseEntity {
    /**
     * 耗材ID（耗材字典的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;

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
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 库存金额
     */
    private BigDecimal totalAmount;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 库存状态（1-正常 2-预警 3-缺货 4-过期）
     */
    private Integer stockStatus;
}
