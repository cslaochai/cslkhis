package com.his.charge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * DRG 细分组（三级目录的最末一层，接入医保局分组方案的接口面）。
 *
 * <p>一行 = 一个可入组的组号，判定依据在 {@link #drgRule} 原文里，规则引用的码集合另表存。
 * 权重与支付标准不由国家方案包下发，属统筹区医保局另行制定的部分，未落地时为 null。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_drg_group")
public class SysDrgGroup extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



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
     * DRG 细分组规则原文
     */
    private String drgRule;

    /**
     * ADRG 内排序
     */
    private Integer sortNo;

    /**
     * 权重（相对权重 RW，统筹区下发前为 null）
     */
    private BigDecimal weight;

    /**
     * 病组支付标准（元，统筹区下发前为 null）
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
