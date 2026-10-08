package com.his.medicaltech.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 国考四指标 VO（M5）—— 统一「近 30 日」窗口，分母一并返回，页面上能对上数。
 */
@Data
public class BiNationalVO {

    /** 统计窗口（天） */
    private Integer windowDays = 30;

    // 平均住院日 / 床位周转

    /** 窗口内出院人数 */
    private Long dischargeCount;

    /** 窗口内出院患者占用总床日 */
    private Long totalBedDays;

    /** 平均住院日（床日/出院人数，2 位小数） */
    private BigDecimal avgLengthOfStay;

    /** 可用床位数（总数-维修） */
    private Long usableBeds;

    /** 床位周转次数（出院人数/可用床位，2 位小数） */
    private BigDecimal bedTurnover;

    // 耗占比

    /** 窗口内收入净额（元） */
    private BigDecimal revenue;

    /** 窗口内耗材材料收入净额（元） */
    private BigDecimal materialRevenue;

    /** 耗占比（0~1，4 位小数） */
    private BigDecimal materialRatio;

    // CMI

    /** CMI 样本病例数（近 30 日出院且主诊断已编码） */
    private Long cmiSampleCount;

    /** 其中入组病例数（QY 不计） */
    private Long cmiGroupedCount;

    /** CMI（Σ权重/样本数，4 位小数；样本为 0 时置 0 并如实返回样本数） */
    private BigDecimal cmi;
}
