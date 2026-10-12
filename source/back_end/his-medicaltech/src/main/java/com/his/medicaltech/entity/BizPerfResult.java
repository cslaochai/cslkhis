package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 科室绩效核算结果实体（同科室同月唯一，重算覆盖，快照自当次成本与系数）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_perf_result")
public class BizPerfResult extends BaseEntity {

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * yyyy-MM
     */
    private String costMonth;

    /**
     * 收费明细净额
     */
    private BigDecimal revenue;

    /**
     * 药品收入
     */
    private BigDecimal drugRevenue;

    /**
     * drug_revenue/revenue
     */
    private BigDecimal drugRatio;

    /**
     * 成本合计
     */
    private BigDecimal totalCost;

    /**
     * 结余 = 收入 - 成本
     */
    private BigDecimal surplus;

    /**
     * 本次核算时点值（默认 0.06）
     */
    private BigDecimal bonusRate;

    /**
     * max(0, 结余) × 系数
     */
    private BigDecimal perfAmount;

    /**
     * 状态（1-草稿 2-已核算 3-已发布）
     */
    private Integer perfStatus;

    /**
     * 成本快照来源
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long costId;

    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;
}
