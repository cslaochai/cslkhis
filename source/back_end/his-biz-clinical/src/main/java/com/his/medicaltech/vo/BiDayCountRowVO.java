package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 近 7 日挂号量趋势点（BiMapper#appointmentTrend 一行）。
 */
@Data
public class BiDayCountRowVO implements Serializable {

    /**
     * 统计日（MM-dd）
     */
    private String statDate;

    /**
     * 当日挂号数
     */
    private Long cnt;
}