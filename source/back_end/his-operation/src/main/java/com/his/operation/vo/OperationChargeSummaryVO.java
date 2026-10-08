package com.his.operation.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 一次计费动作的汇总结果（走到收费之后给调用方的答复）。
 */
@Data
public class OperationChargeSummaryVO implements Serializable {

    /**
     * 本次应计费项数
     */
    private int totalItems = 0;

    /**
     * 实际计入收费单项数
     */
    private int successItems = 0;

    /**
     * 计费失败项数
     */
    private int failedItems = 0;

    /**
     * 本次计入金额（元）
     */
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * 落到的记账单号（费用记账流水的费用编号；失败时可能为空）
     */
    private String feeNo;

    /**
     * 逐项说明（给前端直接展示，不让人去猜为什么总数对不上）
     */
    private List<String> messages = new ArrayList<>();

    /**
     * 是否存在失败项
     */
    public boolean hasFailure() {
        return failedItems > 0;
    }
}
