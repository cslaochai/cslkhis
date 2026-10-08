package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * DRG 分组与权重表（接入医保局的接口面）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drg_group")
public class SysDrgGroup extends BaseEntity {

    /**
     * DRG 组编码（如 FR29）
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
     * 权重（相对权重 RW）
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元）
     */
    private BigDecimal payStandard;

    /**
     * 来源：国家CHS-DRG/省/市版本名称
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
