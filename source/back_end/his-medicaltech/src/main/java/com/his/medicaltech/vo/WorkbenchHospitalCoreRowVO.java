package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工作台数字卡「全院今日概况」（WorkbenchMetricMapper#hospitalCoreStats）。
 */
@Data
public class WorkbenchHospitalCoreRowVO implements Serializable {

    /**
     * 今日挂号数
     */
    private Long todayRegistCount;

    /**
     * 今日实收净额
     */
    private BigDecimal todayRevenue;

    /**
     * 在院患者数
     */
    private Long inHospitalCount;

    /**
     * 床位总数（占用率的分母用它，不是"已占床数"）
     */
    private Long bedTotal;

    /**
     * 占用床位数
     */
    private Long bedOccupied;
}