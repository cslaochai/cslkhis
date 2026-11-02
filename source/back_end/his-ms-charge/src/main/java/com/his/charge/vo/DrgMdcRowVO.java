package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * DRG 主要诊断大类目录一行（分组方案快照加载用，只取判定所需列）。
 */
@Data
public class DrgMdcRowVO implements Serializable {

    /**
     * MDC 编码
     */
    private String mdcCode;

    /**
     * MDC 名称
     */
    private String mdcName;

    /**
     * MDC 入组规则原文（先期分组可为空，能否入组由子 ADRG 决定）
     */
    private String mdcRule;

    /**
     * 是否先期分组（1-是 0-否）
     */
    private Integer preGroupFlag;

    /**
     * 判定步序（1/2/3/4-先期分组，5-普通 MDC）
     */
    private Integer groupStep;

    /**
     * 规则来源（OFFICIAL/INFERRED/LOCAL）
     */
    private String ruleSource;

    /**
     * 是否已复核（1-是 0-否）
     */
    private Integer verified;

    /**
     * 排序（官方判定顺序，先期分组在最前）
     */
    private Integer sortNo;
}
