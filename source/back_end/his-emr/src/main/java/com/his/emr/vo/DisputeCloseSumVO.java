package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 纠纷/投诉：已结案单据的赔偿合计与平均结案天数（未结案不参与平均）。
 */
@Data
public class DisputeCloseSumVO implements Serializable {

    /**
     * 赔偿合计（元）
     */
    private BigDecimal total;

    /**
     * 平均结案天数（受理→结案）
     */
    private BigDecimal avgDays;
}