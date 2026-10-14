package com.mis.vo;

import com.mis.entity.MisAllocation;
import com.mis.entity.JinbMonthlyDeduct;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 月度结算结果（申报→审核→拨付→清算 全量口径，即“应收 vs 实收”）
 */
@Data
public class MonthlyResultVO {

    private String month;
    private Integer status;
    private String statusDesc;
    private String fixMedinsCode;

    private Integer declareCount;
    /** 申报统筹（应收） */
    private BigDecimal declareAmount;
    /** 审核扣款/拒付 */
    private BigDecimal deductAmount;
    /** 质量保证金预留 */
    private BigDecimal reserveAmount;
    /** 本月应拨付 */
    private BigDecimal payableAmount;
    /** 实际已拨付 */
    private BigDecimal allocatedAmount;

    private List<JinbMonthlyDeduct> deducts;
    private List<MisAllocation> allocations;
}
