package com.his.patient.vo;

import lombok.Data;

/**
 * 住院统计卡片 VO
 */
@Data
public class InpatientStatsVO {

    /**
     * 在院患者数
     */
    private long inHospitalCount;

    /**
     * 今日入院数
     */
    private long todayAdmitted;

    /**
     * 今日出院数
     */
    private long todayDischarged;

    /**
     * 待收治住院证数（门诊已开证、入院处还没排床）
     */
    private long pendingAdmissionOrderCount;

    /**
     * 床位总数（实时取自床位）
     */
    private long totalBeds;

    /**
     * 空闲床位数
     */
    private long freeBeds;

    /**
     * 占用床位数
     */
    private long occupiedBeds;

    /**
     * 床位数使用率（%），保留一位小数
     */
    private java.math.BigDecimal bedUsageRate;
}
