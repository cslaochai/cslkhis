package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满意度：答卷总览原始计数（只认有效卷，作废卷单列）。
 */
@Data
public class SurveyOverallStatVO implements Serializable {

    /**
     * 有效卷数
     */
    private Long total;

    /**
     * 五分制均分
     */
    private BigDecimal avgScore;

    /**
     * 百分制均分
     */
    private BigDecimal avgScore100;

    /**
     * 满意卷数（五分制 ≥4）
     */
    private Long satisfied;

    /**
     * 低分卷数（百分制 <60 或有单题 ≤2 分）
     */
    private Long lowScore;

    /**
     * 已转投诉的卷数
     */
    private Long disputed;

    /**
     * 作废卷数
     */
    private Long voided;
}