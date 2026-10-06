package com.his.charge.vo;


import com.his.charge.entity.BizPaymentTxn;
import com.his.charge.entity.BizSettlementBillItem;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单详情：账单头 + 行快照 + 全部收/退流水。
 *
 * <p>三样必须一起给：收费员对着账单核对时，问的就是"这 200 块由哪几笔钱收的、
 * 对应哪几条费用"，缺一样就只能再发三个请求，页面还得自己拼。
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
