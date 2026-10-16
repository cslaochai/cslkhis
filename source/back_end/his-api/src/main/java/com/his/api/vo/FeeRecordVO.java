package com.his.api.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 记账行出参（L1 应收行的对外投影）：调用方只需要定位与留痕字段，
 * 不暴露收费域实体。
 */
@Data
public class FeeRecordVO {

    /**
     * 记账行ID
     */
    private Long id;

    /**
     * 记账行号（唯一，回写到业务单据的 fee_no）
     */
    private String feeNo;

    /**
     * 本行金额（元）
     */
    private BigDecimal amount;
}
