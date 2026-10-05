package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 病区 VO
 * <p>{@code totalBeds/freeBeds/occupiedBeds/brokenBeds} 全部实时统计自床位，
 * <b>不是</b> 病区上那两个演示字段（它们与实际床位行数不符）。
 */
@Data
public class WardVO {

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区编码
     */
    private String wardCode;
    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 所属科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 床位总数（实时取自床位）
     */
    private Integer totalBeds;

    /**
     * 空闲床位数
     */
    private Integer freeBeds;

    /**
     * 已占用床位数
     */
    private Integer occupiedBeds;

    /**
     * 维修床位数
     */
    private Integer brokenBeds;

    /**
     * 使用率（%），保留一位小数
     */
    private java.math.BigDecimal usageRate;
}
