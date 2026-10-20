package com.his.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 退费流水详情出参（含逐条退费明细，明细投影自账单行）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizRefundFlowDetailVO extends BizRefundFlowVO {

    /**
     * 本次冲正的收费明细（退的是哪几条、各退了多少），来自账单行快照
     */
    private List<BizRefundFlowItemVO> details;
}
