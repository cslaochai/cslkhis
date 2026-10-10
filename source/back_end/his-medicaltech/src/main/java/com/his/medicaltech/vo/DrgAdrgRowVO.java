package com.his.medicaltech.vo;

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
     * ADRG 名称（空名称是官方表内的结构占位行，含每 MDC 一条的 x00 空组与各 MDC 的 QY 预入组）
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
}
