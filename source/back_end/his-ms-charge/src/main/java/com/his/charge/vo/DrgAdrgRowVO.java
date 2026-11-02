package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * DRG 核心组目录一行（分组方案快照加载用，只取判定所需列）。
 */
@Data
public class DrgAdrgRowVO implements Serializable {

    /**
     * ADRG 编码
     */
    private String adrgCode;

    /**
     * ADRG 名称（官方 QY/00 伪行没有名字，类型由 row_kind 给出）
     */
    private String adrgName;

    /**
     * ADRG 入组规则原文
     */
    private String adrgRule;

    /**
     * 所属 MDC 编码
     */
    private String mdcCode;

    /**
     * MDC 内排序（官方判定顺序）
     */
    private Integer sortNo;

    /**
     * 行类型（0-正常入组 1-QY 歧义组 2-未入组占位）
     */
    private Integer rowKind;
}
