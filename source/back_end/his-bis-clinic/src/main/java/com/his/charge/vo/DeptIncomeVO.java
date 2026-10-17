package com.his.charge.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 科室收入（日结单的科室归集行）。
 */
@Data
public class DeptIncomeVO {

    /**
     * 科室ID（null 表示"无科室归属"聚合行）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 明细笔数
     */
    private Integer itemCount;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 占比（%，保留 2 位）。分母是**全部明细金额**（含无归属），
     * 否则无归属那一块会让所有科室占比加起来超过 100%。
     */
    private BigDecimal ratio;

    /**
     * 是否为"无科室归属"聚合行
     */
    private Boolean unattributed;
}
