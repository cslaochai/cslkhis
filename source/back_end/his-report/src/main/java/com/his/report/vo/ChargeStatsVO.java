package com.his.report.vo;

import com.his.charge.vo.BizSettlementBillVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收费统计出参（四层口径：基于结算账单结算账单）
 */
@Data
public class ChargeStatsVO {

    /**
     * 已支付账单数量
     */
    private Long totalCount;

    /**
     * 收费总金额（已支付账单应收合计），单位：元
     */
    private BigDecimal totalAmount;

    /**
     * 已支付账单列表
     */
    private List<BizSettlementBillVO> bills;
}
