package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 耗材科室领用台账（耗材科室领用台账）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_consumable_consume")
public class BizConsumableConsume extends BaseEntity {
    /**
     * 领用单号
     */
    private String consumeNo;

    /**
     * 耗材ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long consumableId;

    /**
     * 耗材名称（快照）
     */
    private String consumableName;

    /**
     * 规格（快照）
     */
    private String specification;

    /**
     * 单位（快照）
     */
    private String unit;

    /**
     * 领用数量
     */
    private BigDecimal quantity;

    /**
     * 领用科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 领用科室名称（快照）
     */
    private String deptName;

    /**
     * 用途
     */
    private String purpose;

    /**
     * 领用时间
     */
    private LocalDateTime consumeTime;

    /**
     * 经办人
     */
    private String operatorName;

    /**
     * 领用前该耗材全部批次合计
     */
    private BigDecimal stockBefore;

    /**
     * 领用后该耗材全部批次合计
     */
    private BigDecimal stockAfter;
}
