package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DRG 组表一行（{@code DrgSimMapper#groupList}，CHS-DRG 1.1模拟种子）。
 *
 * <p>与 DRG 模拟页/国考 CMI 同源：两边都必须读这一张表，否则"模拟页算出的盈亏"与
 * "国考 CMI"会用两套权重，谁也说不清哪个对。
 */
@Data
public class DrgGroupRowVO implements Serializable {

    /**
     * 组表主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * DRG 组编码
     */
    private String drgCode;

    /**
     * DRG 组名称
     */
    private String drgName;

    /**
     * MDC 主要诊断大类
     */
    private String mdcCode;

    /**
     * ADRG 编码
     */
    private String adrgCode;

    /**
     * 组权重
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元）
     */
    private BigDecimal payStandard;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}