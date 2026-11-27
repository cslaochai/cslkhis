package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室收入 TOP5 一行（BiMapper#deptTop）。
 */
@Data
public class BiDeptAmountRowVO implements Serializable {

    /**
     * 科室 ID（未分配科室为 null），供驾驶舱下钻到科室明细报表
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称（未分配时为「未分配科室」）
     */
    private String deptName;

    /**
     * 近 30 日收入净额
     */
    private BigDecimal amount;
}