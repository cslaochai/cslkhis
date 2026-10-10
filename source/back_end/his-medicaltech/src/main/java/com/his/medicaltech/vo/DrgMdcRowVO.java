package com.his.medicaltech.vo;

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
     * MDC 名称（空名称是官方表内的结构占位行）
     */
    private String mdcName;

    /**
     * MDC 入组规则原文
     */
    private String mdcRule;

    /**
     * 排序（官方判定顺序，先期分组在最前）
     */
    private Integer sortNo;
}
