package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * 科室归集聚合（一个科室一行），对应
 */
@Data
public class DeptAmountSumVO implements Serializable {

    /**
     * 科室ID（SQL 已过滤掉 NULL，无科室归属的行不进这里）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（取组内MAX，摊行未带名称时为 null）
     */
    private String deptName;

    /**
     * 摊行笔数（{@code COUNT(*)}；MySQL BIGINT）
     */
    private Long cnt;

    /**
     * 摊行金额合计（元，毛收入，未扣优惠/统筹）
     */
    private BigDecimal amount;
}