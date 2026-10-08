package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 科室月度收入（PerfMapper#sumDeptRevenue）。
 */
@Data
public class PerfDeptRevenueRowVO implements Serializable {

    /**
     * 科室名称快照（该科室没有流水时为「未分配科室」）
     */
    private String deptName;

    /**
     * 月度收入净额
     */
    private BigDecimal revenue;

    /**
     * 药品收入净额（item_type 2西药 3中成药 4中药饮片）
     */
    private BigDecimal drugRevenue;
}