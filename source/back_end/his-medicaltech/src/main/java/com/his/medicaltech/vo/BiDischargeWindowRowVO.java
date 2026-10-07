package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 近 30 日出院队列窗口（{@code BiMapper#dischargeWindow30d}）。
 *
 * <p>床日 = {@code TIMESTAMPDIFF(DAY, admit_time, discharge_time)}，当天入当天出按 1 计
 * （GREATEST 兜底）；discharge_time 回填缺失的脏行不计入，否则床日会算成 NULL把均值拖歪。
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