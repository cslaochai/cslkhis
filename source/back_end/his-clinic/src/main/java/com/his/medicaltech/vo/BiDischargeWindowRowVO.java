package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 近 30 日出院队列窗口（BiMapper#dischargeWindow30d）。
 */
@Data
public class BiDischargeWindowRowVO implements Serializable {

    /**
     * 出院人数
     */
    private Long dischargeCount;

    /**
     * 占用总床日
     */
    private Long bedDays;
}