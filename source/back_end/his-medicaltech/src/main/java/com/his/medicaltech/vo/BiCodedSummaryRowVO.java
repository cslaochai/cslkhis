package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * CMI样本：近 30 日出院且主诊断已编码的病案首页一行（BiMapper#codedSummaries30d）。
 */
@Data
public class BiCodedSummaryRowVO implements Serializable {

    /**
     * 主要诊断ICD 编码
     */
    private String icdCode;

    /**
     * 是否手术（0-否 1-是）
     */
    private Integer isSurgery;

    /**
     * 住院天数
     */
    private Integer inpatientDays;

    /**
     * 死亡标志（0-否 1-是）
     */
    private Integer deathFlag;
}