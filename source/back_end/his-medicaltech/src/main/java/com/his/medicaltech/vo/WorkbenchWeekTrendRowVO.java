package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台折线卡的一日一点（WorkbenchMetricMapper#weekRegistTrend 一行）。
 */
@Data
public class WorkbenchWeekTrendRowVO implements Serializable {

    /**
     * 就诊日期（yyyy-MM-dd）
     */
    private String date;

    /**
     * 当日挂号数
     */
    private Long cnt;
}