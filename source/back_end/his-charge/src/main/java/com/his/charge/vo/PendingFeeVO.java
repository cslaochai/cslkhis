package com.his.charge.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算台首屏：待结算费用 + 本次合计 + 该就诊下已出账单还欠多少。
 */
@Data
public class PendingFeeVO {

    private List<BizFeeRecordVO> fees;

    /**
     * 待结算费用净额（含红冲负行）
     */
    private BigDecimal totalAmount;

    /**
     * 已出账单尚未收讫的应缴差额
     */
    private BigDecimal unpaidBillAmount;
}
