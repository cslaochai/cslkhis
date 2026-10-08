package com.his.charge.vo;


import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBillItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 账单详情：账单头 + 行快照 + 全部收/退流水。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizSettlementBillDetailVO extends BizSettlementBillVO {

    /**
     * 明细项集合
     */
    private List<BizSettlementBillItem> items;

    private List<BizPaymentTxn> txns;
}
