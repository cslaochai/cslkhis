package com.his.charge.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DRG 分组方案头一行（方案快照装载与健康检查用）。
 */
@Data
public class DrgSchemeRowVO implements Serializable {

    /**
     * 分组方案版本（与目录表的 version 同值）
     */
    private String version;

    /**
     * 方案编码
     */
    private String schemeCode;

    /**
     * 方案名称
     */
    private String schemeName;

    /**
     * 标准文号
     */
    private String standardNo;

    /**
     * 官方发布日期
     */
    private LocalDate publishDate;

    /**
     * 统筹区执行日期
     */
    private LocalDate effectiveDate;

    /**
     * 规则载体
     */
    private String sourceFile;

    /**
     * 规则来源（OFFICIAL/INFERRED/LOCAL）
     */
    private String ruleSource;

    /**
     * 是否已复核（1-是 0-否）
     */
    private Integer verified;

    /**
     * 官方 MDC 真组数
     */
    private Integer expectedMdc;

    /**
     * 官方 ADRG 真组数
     */
    private Integer expectedAdrg;

    /**
     * 官方 DRG 真组数
     */
    private Integer expectedDrg;

    /**
     * 状态（0-草稿 1-启用 2-停用）
     */
    private Integer status;
}
