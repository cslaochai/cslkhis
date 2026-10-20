package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台数字卡「病区今日概况」（WorkbenchMetricMapper#nurseStats）。
 */
@Data
public class WorkbenchNurseStatsRowVO implements Serializable {

    /**
     * 病区在院人数
     */
    private Long wardInpatientCount;

    /**
     * 病区床位总数
     */
    private Long bedTotal;

    /**
     * 病区占用床位数
     */
    private Long bedOccupied;

    /**
     * 今日入院人数
     */
    private Long todayAdmitCount;

    /**
     * 今日出院人数
     */
    private Long todayDischargeCount;

    /**
     * 待执行医嘱数
     */
    private Long todoExecCount;
}