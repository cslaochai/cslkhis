package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 处方点评：月度点评原始计数（按明细快照 visit_date 聚合）。
 *
 * <p>点评率的分母（同期已审核/已发药处方总数）不在这里 —— 它数的是处方表，
 * 不是点评明细表，两张表的口径差在别的 SQL 里。
 */
@Data
public class RxReviewMonthStatVO implements Serializable {

    /**
     * 已点评条数
     */
    private Long reviewed;

    /**
     * 不合理条数（点评结论 2/3/4）
     */
    private Long unreasonable;

    /**
     * 异常条数（点评结论 4）
     */
    private Long abnormal;

    /**
     * 已公示条数
     */
    private Long publicized;

    /**
     * 待点评条数（点评状态 0）
     */
    private Long pending;
}