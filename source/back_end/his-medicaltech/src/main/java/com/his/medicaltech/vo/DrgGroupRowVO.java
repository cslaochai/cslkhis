package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DRG 细分组一行（入组维度只有规则原文，其余维度都能从规则里推出来）。
 */
@Data
public class DrgGroupRowVO implements Serializable {

    /**
     * 细分组主键
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
     * DRG 细分组规则原文（空=该 ADRG 下的兜底档，如「不伴合并症或并发症」）
     */
    private String drgRule;

    /**
     * ADRG 内排序（官方判定顺序，MCC 档在 CC 档之前）
     */
    private Integer sortNo;

    /**
     * 组权重（统筹区医保局下发，官方 3.0 包不含，未下发为 null）
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元，同上）
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
